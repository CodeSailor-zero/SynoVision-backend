package com.sean.synovision.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.synovision.model.dto.picture.PictureQueryRequest;
import com.sean.synovision.model.dto.picture.PictureReviewRequest;
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

    /**
     * 上传图片功能
     * @param multipartFile 图片
     * @param pictureUploadRequest 图片上传请求封装
     * @param user 当前用户
     * @return 图片的封装类
     */
    PictureVo uploadPicture(Object multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User user
    );

    /**
     * 图片审核功能
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest,User loginUser);

    /**
     * 分页查询图片
     * @param pictureQueryRequest
     * @return
     */
    Page<Picture> listPicturePage(PictureQueryRequest pictureQueryRequest);

    /**
     * 查询所有图片
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    Page<PictureVo> listPictureVoPage(PictureQueryRequest pictureQueryRequest,HttpServletRequest request);


    void fullPictureReviewPramas(Picture picture, User loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    PictureVo getPictureVo(Picture picture, HttpServletRequest request);

    Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request);
}
