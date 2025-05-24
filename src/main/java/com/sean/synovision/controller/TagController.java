package com.sean.synovision.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.synovision.annotation.AuthCheck;
import com.sean.synovision.common.BaseResponse;
import com.sean.synovision.common.DeleteRequest;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.exception.ResultUtils;
import com.sean.synovision.model.dto.tag.TagAddRequest;
import com.sean.synovision.model.dto.tag.TagQueryRequest;
import com.sean.synovision.model.dto.tag.TagUpdateRequest;
import com.sean.synovision.model.entity.Tag;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.picture.PictureTagCategeoy;
import com.sean.synovision.model.vo.tag.TagVo;
import com.sean.synovision.service.TagService;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author sean
 * @Date 2025/12/24
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private UserService userService;

    @Resource
    private TagService tagService;

    /**
     * 添加tag(只能添加一条tag)
     * @param tagAddRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> addTag (@RequestBody TagAddRequest tagAddRequest,
                                      HttpServletRequest request) {
        ThrowUtill.throwIf(tagAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(tagService.addTag(tagAddRequest, loginUser));
    }

    /**
     * 删除tag
     * @param deleteRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTag (@RequestBody DeleteRequest deleteRequest,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtill.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        return ResultUtils.success(tagService.deleteTag(id));
    }
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTag (@RequestBody TagUpdateRequest tagUpdateRequest,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtill.throwIf(tagUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(tagService.updateTag(tagUpdateRequest));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list")
    public BaseResponse<Page<Tag>> listTagByPage (@RequestBody TagQueryRequest tagQueryRequest,
                                                  HttpServletRequest request) {
        ThrowUtill.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(tagService.fetchTagPage(tagQueryRequest, request));
    }
}
