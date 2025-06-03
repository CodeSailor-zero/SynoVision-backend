package com.sean.synovision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import com.sean.synovision.common.BaseResponse;
import com.sean.synovision.common.DeleteRequest;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.exception.ResultUtils;
import com.sean.synovision.manager.auth.annotation.SaSpaceCheckPermission;
import com.sean.synovision.manager.auth.model.SpaceUserPermissionConstant;
import com.sean.synovision.model.dto.space.SpaceEditRequest;
import com.sean.synovision.model.dto.space.SpaceQueryRequest;
import com.sean.synovision.model.dto.spaceMember.SpaceMemberAddRequest;
import com.sean.synovision.model.dto.spaceMember.SpaceMemberEditRequest;
import com.sean.synovision.model.dto.spaceMember.SpaceMemberQueryRequest;
import com.sean.synovision.model.entity.SpaceMember;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.spaceMember.SpaceMemberVo;
import com.sean.synovision.service.SpaceMemberService;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author sean
 * @Date 2025/06/02
 */
@Slf4j
@RequestMapping("/spaceMember")
@RestController
public class SpaceMemberController {
    @Resource
    private SpaceMemberService spaceMemberService;

    @Resource
    private UserService userService;

    /**
     * 添加成员到空间
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceMemberAddRequest spaceMemberAddRequest) {
        ThrowUtill.throwIf(spaceMemberAddRequest == null, ErrorCode.PARAMS_ERROR);
        long id = spaceMemberService.addSpaceMember(spaceMemberAddRequest);
        return ResultUtils.success(id);
    }

    /**
     * 从空间移除成员
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest,
                                                 HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        SpaceMember oldSpaceUser = spaceMemberService.getById(id);
        ThrowUtill.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceMemberService.removeById(id);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询某个成员在某个空间的信息
     */
    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceMember> getSpaceUser(@RequestBody SpaceMemberQueryRequest spaceMemberQueryRequest) {
        // 参数校验
        ThrowUtill.throwIf(spaceMemberQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceMemberQueryRequest.getSpaceId();
        Long userId = spaceMemberQueryRequest.getUserId();
        ThrowUtill.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceMember spaceMember = spaceMemberService.getOne(spaceMemberService
                .getQueryWrapper(spaceMemberQueryRequest));
        ThrowUtill.throwIf(spaceMember == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceMember);
    }

    /**
     * 查询成员信息列表
     */
    @PostMapping("/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceMemberVo>> listSpaceUser(@RequestBody SpaceMemberQueryRequest spaceMemberQueryRequest,
                                                           HttpServletRequest request) {
        ThrowUtill.throwIf(spaceMemberQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceMember> spaceUserList = spaceMemberService.list(
                spaceMemberService.getQueryWrapper(spaceMemberQueryRequest)
        );
        return ResultUtils.success(spaceMemberService.getSpaceMemberVoList(spaceUserList));
    }

    /**
     * 编辑成员信息（设置权限）
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceMemberEditRequest spaceMemberEditRequest,
                                               HttpServletRequest request) {
        if (spaceMemberEditRequest == null || spaceMemberEditRequest.getId() <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        SpaceMember spaceMember = new SpaceMember();
        BeanUtils.copyProperties(spaceMemberEditRequest, spaceMember);
        // 数据校验
        spaceMemberService.vaildSpaceMember(spaceMember, false);
        // 判断是否存在
        long id = spaceMemberEditRequest.getId();
        SpaceMember oldSpaceUser = spaceMemberService.getById(id);
        ThrowUtill.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceMemberService.updateById(spaceMember);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询我加入的团队空间列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceMemberVo>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceMemberQueryRequest spaceMemberQueryRequest = new SpaceMemberQueryRequest();
        spaceMemberQueryRequest.setUserId(loginUser.getId());
        List<SpaceMember> spaceUserList = spaceMemberService.list(
                spaceMemberService.getQueryWrapper(spaceMemberQueryRequest)
        );
        return ResultUtils.success(spaceMemberService.getSpaceMemberVoList(spaceUserList));
    }
}
