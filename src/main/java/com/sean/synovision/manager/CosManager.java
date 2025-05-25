package com.sean.synovision.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.sean.synovision.config.CosConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author sean
 * @Date 2025/04/21
 */
@Component
public class CosManager {
    @Resource
    private CosConfig cosConfig;
    @Resource
    private COSClient cosClient;

    /**
     * 将本地文件上传到 COS
     * @param key 文件名
     * @param file 本地文件
     * @return
     * @throws CosClientException
     * @throws CosServiceException
     */
    public PutObjectResult putObject(String key, File file)
            throws CosClientException, CosServiceException {
        return cosClient.putObject(cosConfig.getBucketName(), key, file);
    }

    /**
     * 下载对象
     * @param key
     * @return
     */

    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosConfig.getBucketName(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * <a href="https://cloud.tencent.com/document/product/436/116860" />
     * 将本地文件上传到 COS（并且对文件进行处理）,这里需要你开通腾讯云的数据万象
     * @param key 文件名
     * @param file 本地文件
     * @return
     * @throws CosClientException
     * @throws CosServiceException
     */
    public PutObjectResult putPictureObject(String key, File file)
            throws CosClientException, CosServiceException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucketName(), key, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

}
