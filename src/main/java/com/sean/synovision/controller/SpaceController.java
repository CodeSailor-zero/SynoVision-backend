package com.sean.synovision.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.synovision.annotation.AuthCheck;
import com.sean.synovision.common.BaseResponse;
import com.sean.synovision.common.DeleteRequest;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.exception.ResultUtils;
import com.sean.synovision.model.dto.space.SpaceAddRequest;
import com.sean.synovision.model.dto.space.SpaceEditRequest;
import com.sean.synovision.model.dto.space.SpaceQueryRequest;
import com.sean.synovision.model.dto.space.SpaceUpdateRequest;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.space.SpaceVo;
import com.sean.synovision.service.PictureService;
import com.sean.synovision.service.SpaceService;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sean
 * @Date 2025/05/26
 */
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceService spaceService;

    //region 增删改查
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Space oldSpace = spaceService.getById(id);
        ThrowUtill.throwIf(oldSpace == null, ErrorCode.PARAMS_ERROR, "当前空间不存在");
        //权限校验
        spaceService.checkSpaceAuth(loginUser, oldSpace);
        //查询与空间关联的图片
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<Picture>()
                .select("id")
                .eq("spaceId", id);
        List<Long> pictureIdList = pictureService.list(pictureQueryWrapper)
                .stream()
                .map(Picture::getId)
                .collect(Collectors.toList());
        ThrowUtill.throwIf(CollectionUtil.isEmpty(pictureIdList), ErrorCode.SYSTEM_ERROR, "当前空间下没有图片，无需删除");
        transactionTemplate.execute(status -> {
            // 删除空间内部的所有图片。cos图片清理交给定时任务
            boolean result = spaceService.removeById(id);
            ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR, "空间删除失败");
            boolean res = pictureService.removeByIds(pictureIdList);
            ThrowUtill.throwIf(!res, ErrorCode.OPERATION_ERROR, "空间内部图片失败");
            return null;
        });
        return ResultUtils.success(true);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(spaceUpdateRequest == null || spaceUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        //补充空间参数
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.vaildSpace(space, false);
        Space oldSpace = spaceService.getById(space.getId());
        ThrowUtill.throwIf(oldSpace == null, ErrorCode.PARAMS_ERROR, "当前空间不存在");
        //补充图片审核校验参数
        User loginUser = userService.getLoginUser(request);
        boolean result = spaceService.updateById(space);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR, "空间更新失败");
        return ResultUtils.success(true);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<Space> getSpace(Long id, HttpServletRequest request) {
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Space space = spaceService.getById(id);
        ThrowUtill.throwIf(space == null, ErrorCode.PARAMS_ERROR, "当前空间不存在");
        return ResultUtils.success(space);
    }

    @GetMapping("/get/vo")
    public BaseResponse<SpaceVo> getSpaceVo(Long id, HttpServletRequest request) {
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Space space = spaceService.getById(id);
        ThrowUtill.throwIf(space == null, ErrorCode.PARAMS_ERROR, "当前空间不存在");
        //
        SpaceVo spaceVo = spaceService.getSpaceVo(space, request);
        return ResultUtils.success(spaceVo);
    }

    @PostMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpace(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        Page<Space> picturePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(picturePage);
    }

    @PostMapping("/list/vo")
    public BaseResponse<Page<SpaceVo>> listSpaceVo(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        BaseResponse<Page<Space>> pageBaseResponse = listSpace(spaceQueryRequest);
        Page<Space> spacePage = pageBaseResponse.getData();
        Page<SpaceVo> spaceVoPage = spaceService.getSpaceVoPage(spacePage, request);
        return ResultUtils.success(spaceVoPage);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtill.throwIf(spaceEditRequest == null || spaceEditRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        //补充空间参数
        spaceService.fillSpaceBySpaceLevel(space);
        space.setEditTime(new Date());
        //picture 参数校验
        spaceService.vaildSpace(space, false);
        Space oldSpace = spaceService.getById(space.getId());
        ThrowUtill.throwIf(oldSpace == null, ErrorCode.PARAMS_ERROR, "当前空间不存在");
        //权限校验
        spaceService.checkSpaceAuth(loginUser, space);
        boolean result = spaceService.updateById(space);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR, "空间更新失败");
        return ResultUtils.success(true);
    }
    // endregion
}
