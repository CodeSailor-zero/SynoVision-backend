package com.sean.synovision.manager.upload;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.sean.synovision.config.CosConfig;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.manager.CosManager;
import com.sean.synovision.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author sean
 * @Date 2025/05/25
 * 上传图片模板
 */
@Slf4j
public abstract class UploadPictureTemplate {
    @Resource
    private CosConfig cosConfig;
    @Resource
    private COSClient cosClient;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片（对图片进行严格的校验）
     *
     * @param inputSource 文件
     * @param prefix        文件前缀
     * @return 上传结果
     */

    public UploadPictureResult uploadFile(Object inputSource, String prefix) {
        //1. 校验文件
        validFile(inputSource);
        //2. 上传路径定义
        //2.1 定义文件名：当前时间 +  uuid + 文件后缀
        String date = DateUtil.formatDate(new Date());
        UUID uuid = UUID.randomUUID();
        String originalFilename = getOriginalFilename(inputSource);
        String suffix = FileUtil.getSuffix(originalFilename);
        String uploadFileName = String.format("%s_%s.%s", date, uuid, suffix);
        //2.2 定义上传到腾讯云cos的路径：/用户传的前缀 + /文件名
        String filePath = String.format("/%s/%s", prefix, uploadFileName);
        File file = null;
        try {
            //3. 将文件保存到本地
            file = File.createTempFile(filePath, null);
            processFile(inputSource,file);

            //4. 上传图片到腾讯云cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            //5. 返回封装结果
            //5.1 获取图片信息结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 获取原始的图片 url
            URL originalUrl = cosClient.getObjectUrl(cosConfig.getBucketName(), filePath);
            // 获取处理后的结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> pictureList = processResults.getObjectList();
            if (CollectionUtil.isNotEmpty(pictureList)) {
                // 获取压缩后的图片信息（webp格式的文件）
                CIObject picture = pictureList.get(0);
                // 如果缩略图不存在，则默认等于压缩图
                CIObject thumbnailPicture = picture;
                if (pictureList.size() > 1) {
                    // 获取缩略后的图片信息
                    thumbnailPicture = pictureList.get(1);
                }
                return buildResult(originalFilename, picture,thumbnailPicture,originalUrl);
            }

            //5.2 返回封装结果
            return buildResult(imageInfo, filePath, originalFilename, file);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //清理文件
            deleteFile(file, filePath);
        }
    }

    /**
     * 校验参数
     * @param inputSource
     */
    protected abstract void validFile(Object inputSource);

    /**
     * 得到原始文件名字
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 生成本地临时文件
     * @param inputSource
     */
    protected abstract void processFile(Object inputSource,File file) throws IOException;

    /**
     * 封装上传结果
     *
     * @param originalFilename 原始文件名
     * @param picture          webp 文件信息
     * @param thumbnailPicture 缩略图文件信息
     * @param originalUrl
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject picture, CIObject thumbnailPicture, URL originalUrl) {
        Integer picWidth = picture.getWidth();
        Integer picHeight = picture.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        Integer size = picture.getSize();
        String format = picture.getFormat();


        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        //设置压缩后的图地址
        uploadPictureResult.setUrl(cosConfig.getHost() + "/" + picture.getKey());
        //设置缩略后的图地址
        uploadPictureResult.setThumbnailUrl(cosConfig.getHost() + "/" + thumbnailPicture.getKey());
        //设置原图地址
        uploadPictureResult.setOriginalUrl(originalUrl.toString());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(size);
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(format);

        return uploadPictureResult;
    }

    /**
     * 封装上传结果
     * @param imageInfo
     * @param filePath
     * @param originalFilename
     * @param file
     * @return
     */
    public UploadPictureResult buildResult(ImageInfo imageInfo,String filePath,String originalFilename,File file){
        // 封装我们返回结果
        String format = imageInfo.getFormat();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();


        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosConfig.getHost() + "/" + filePath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(format);

        return uploadPictureResult;
    }


    /**
     * 删除本地文件
     * @param file
     * @param filePath
     */
    public void deleteFile(File file, String filePath) {
        if (file != null) {
            boolean delete = file.delete();
            if (!delete) {
                log.error("文件删除失败，fillPath = {}", filePath);
            }
        }
    }

    /**
     * 生成预签名 URL
     * @param filePath
     * @return
     */
    @Deprecated
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
