package com.sean.synovision.manager;

import cn.hutool.core.io.FileUtil;
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
import java.util.ArrayList;
import java.util.List;

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
        //设置获取文件信息
        picOperations.setIsPicInfo(1);
        //文件压缩(转为 webp格式) - 添加规则
        List<PicOperations.Rule> ruleList = new ArrayList<>();
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setFileId(webpKey);
        rule.setRule("imageMogr2/format/webp");
        rule.setBucket(cosConfig.getBucketName());
        ruleList.add(rule);
        //添加缩放规则 - 文件大小大于 20KB 才可以进行缩放【在主页展示缩略图】
        if (file.length() > 2 * 1024) {
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s",256,256));
            thumbnailRule.setBucket(cosConfig.getBucketName());
            ruleList.add(thumbnailRule);
        }
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

}
