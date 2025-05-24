package com.sean.synovision.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.synovision.model.dto.picture.PictureQueryRequest;
import com.sean.synovision.model.dto.picture.PictureUploadRequest;
import com.sean.synovision.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.picture.PictureVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 24395
* @description 针对表【picture(图片表)】的数据库操作Service
* @createDate 2025-05-21 11:20:34
*/
public interface PictureService extends IService<Picture> {

    void vaildPicture(Picture picture);

    PictureVo uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User user
    );

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    PictureVo getPictureVo(Picture picture, HttpServletRequest request);

// 根据传入的图片分页对象和请求对象，返回图片视图对象的分页对象
    Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request);
}
