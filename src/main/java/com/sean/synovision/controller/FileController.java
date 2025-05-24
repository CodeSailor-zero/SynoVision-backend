package com.sean.synovision.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.sean.synovision.annotation.AuthCheck;
import com.sean.synovision.common.BaseResponse;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.exception.ResultUtils;
import com.sean.synovision.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author sean
 * @Date 2025/09/21
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUpload(@RequestPart("file") MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", filename);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("文件删除失败，fillPath = {}", filePath);
                }
            }
        }
    }


    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/download")
    public void testDownload(String filePath, HttpServletResponse response) throws IOException {
        COSObjectInputStream objectContent = null;
        try {
            COSObject cosObject = cosManager.getObject(filePath);
            objectContent = cosObject.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(objectContent);
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new RuntimeException(e);
        } finally {
            if (objectContent != null) {
                objectContent.close();
            }
        }
    }
}
