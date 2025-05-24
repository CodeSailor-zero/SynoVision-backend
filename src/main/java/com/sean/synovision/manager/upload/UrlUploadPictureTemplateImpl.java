package com.sean.synovision.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author sean
 * @Date 2025/55/25
 */
@Service
public class UrlUploadPictureTemplateImpl extends UploadPictureTemplate {
    @Override
    protected void validFile(Object inputSource) {
        String fileUrl = (String) inputSource;
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

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.getName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        // 下载图片到本地
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }
}
