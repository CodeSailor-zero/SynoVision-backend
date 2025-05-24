package com.sean.synovision.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author sean
 * @Date 2025/46/25
 */
@Service
public class FileUploadPictureTemplateImpl extends UploadPictureTemplate {
    @Override
    protected void validFile(Object inputSource) {
        MultipartFile multipartFile =  (MultipartFile) inputSource;
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtill.throwIf(fileSize > ONE_M * 2, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
        // 校验文件类型
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> SUFFIXLIST = Arrays.asList("png", "jpg", "jpeg", "webp");
        ThrowUtill.throwIf(!SUFFIXLIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile =  (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile =  (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
