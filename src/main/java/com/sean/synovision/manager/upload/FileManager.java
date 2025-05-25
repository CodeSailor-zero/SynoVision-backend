package com.sean.synovision.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.sean.synovision.config.CosConfig;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.manager.CosManager;
import com.sean.synovision.model.dto.file.UploadPictureResult;
import com.sean.synovision.utill.ThrowUtill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
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
//@Service
@Deprecated
public class FileManager {
    @Resource
    private CosConfig cosConfig;
    @Resource
    private COSClient cosClient;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片（对图片进行严格的校验）
     *
     * @param multipartFile 文件
     * @param prefix        文件前缀
     * @return 上传结果
     */

    public UploadPictureResult uploadFile(MultipartFile multipartFile, String prefix) {
        // 校验文件
        validFile(multipartFile);
        // 上传文件
        // 文件名 ：当前时间 +  uuid + 文件后缀
        String date = DateUtil.formatDate(new Date());
        UUID uuid = UUID.randomUUID();
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        String uploadFileName = String.format("%s_%s.%s", date, uuid, suffix);
        // 定义上传的路径
        String filePath = String.format("/%s/%s", prefix, uploadFileName);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            // 返回文件结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装我们返回结果
            String format = imageInfo.getFormat();
            int picWidth = imageInfo.getWidth();
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

    /**
     * 上传图片通过URL
     *
     * @param url
     * @param prefix
     * @return
     */

    public UploadPictureResult uploadFileByUrl(String url, String prefix) {
        // 校验文件
        validUrl(url);
        // 上传文件
        // 文件名 ：当前时间 +  uuid + 文件后缀
        String date = DateUtil.formatDate(new Date());
        UUID uuid = UUID.randomUUID();
        String originalFilename = FileUtil.mainName(url);
        String suffix = FileUtil.getSuffix(originalFilename);
        String uploadFileName = String.format("%s_%s.%s", date, uuid, suffix);
        // 定义上传的路径
        String filePath = String.format("/%s/%s", prefix, uploadFileName);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            // 下载图片到本地
            HttpUtil.downloadFile(url, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            // 返回文件结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装我们返回结果
            String format = imageInfo.getFormat();
            int picWidth = imageInfo.getWidth();
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

    /**
     * 根据 url 校验文件
     *
     * @param fileUrl
     */
    private void validUrl(String fileUrl) {
        //1. 校验参数
        ThrowUtill.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "url不能为空");
        //2. 校验 url 的格式
        ThrowUtill.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")
                , ErrorCode.PARAMS_ERROR, "url格式不正确");
        //3. 校验url是否合法
        try {
            URL url = new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        //4. 发送 head 请求，获取文件信息，校验文件信息
        HttpResponse response = null;
        try {
            response = HttpUtil
                    .createRequest(Method.HEAD, fileUrl)
                    .execute();
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            //4.1 文件类型的校验
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                List<String> fileContentType = Arrays.asList("image/png", "image/jpeg", "image/jpg", "image/webp");
                ThrowUtill.throwIf(!fileContentType.contains(contentType), ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            //4.2 文件大小的校验
            String contentLength = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)) {
                try {
                    long fileSize = Long.parseLong(contentLength);
                    ThrowUtill.throwIf(fileSize > 2 * 1024 * 1024, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            if (response != null) {
                response.close();
                ;
            }
        }
    }

    private void validFile(MultipartFile multipartFile) {
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtill.throwIf(fileSize > ONE_M * 2, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
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
