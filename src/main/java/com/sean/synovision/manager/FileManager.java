package com.sean.synovision.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.sean.synovision.config.CosConfig;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.model.dto.file.UploadPictureResult;
import com.sean.synovision.utill.ThrowUtill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author sean
 * @Date 2025/04/21
 */
@Slf4j
@Service
public class FileManager {
    @Resource
    private CosConfig cosConfig;
    @Resource
    private COSClient cosClient;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片（对图片进行严格的校验）
     * @param multipartFile 文件
     * @param prefix 文件前缀
     * @return 上传结果
     */

    public UploadPictureResult uploadFile(MultipartFile multipartFile, String prefix) {
        // 校验文件
        validFile(multipartFile, prefix);
        // 上传文件
        // 文件名 ：当前时间 +  uuid + 文件后缀
        String date = DateUtil.formatDate(new Date());
        UUID uuid = UUID.randomUUID();
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        String uploadFileName = String.format("%s_%s.%s", date, uuid, suffix);
        // 定义上传的路径
        String filePath = String.format("/%s/%s",prefix,uploadFileName);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            // 返回文件结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装我们返回结果
            String format = imageInfo.getFormat();
            int  picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

            // 生成预签名 URL
            String accessUrl = getPresignedUrl(filePath);

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(accessUrl);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(format);

            return uploadPictureResult;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //清理文件
            deleteFile(file, filePath);
        }
    }

    private void validFile(MultipartFile multipartFile, String fileSux) {
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtill.throwIf(fileSize > ONE_M * 2, ErrorCode.PARAMS_ERROR ,"文件大小不能超过2M");
        // 校验文件类型
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> SUFFIXLIST = Arrays.asList("png", "jpg", "jpeg", "webp");
        ThrowUtill.throwIf(!SUFFIXLIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }
    public void deleteFile(File file, String filePath) {
        if (file != null) {
            boolean delete = file.delete();
            if (!delete) {
                log.error("文件删除失败，fillPath = {}", filePath);
            }
        }
    }

    public String getPresignedUrl(String filePath) {
        // 使用腾讯云 COS 的 SDK 生成预签名 URL
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(cosConfig.getBucketName(), filePath);
        //设置过期时间：
        Date expirationDate = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);
        request.setExpiration(expirationDate);
        URL url = cosClient.generatePresignedUrl(request);
        return url.toString();
    }
}
