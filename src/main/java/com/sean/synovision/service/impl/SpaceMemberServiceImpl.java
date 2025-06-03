package com.sean.synovision.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.model.dto.spaceMember.SpaceMemberAddRequest;
import com.sean.synovision.model.dto.spaceMember.SpaceMemberQueryRequest;
import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.entity.SpaceMember;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.enums.SpaceRoleEnum;
import com.sean.synovision.model.vo.space.SpaceVo;
import com.sean.synovision.model.vo.spaceMember.SpaceMemberVo;
import com.sean.synovision.model.vo.user.UserVo;
import com.sean.synovision.service.SpaceMemberService;
import com.sean.synovision.mapper.SpaceMemberMapper;
import com.sean.synovision.service.SpaceService;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 24395
 * @description 针对表【spacemember(空间成员表)】的数据库操作Service实现
 * @createDate 2025-06-02 15:30:36
 */
@Service
public class SpaceMemberServiceImpl extends ServiceImpl<SpaceMemberMapper, SpaceMember>
        implements SpaceMemberService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private SpaceService spaceService;

    @Override
    public long addSpaceMember(SpaceMemberAddRequest spaceMemberAddRequest) {
        // 参数校验
        ThrowUtill.throwIf(spaceMemberAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceMember spaceMember = new SpaceMember();
        BeanUtils.copyProperties(spaceMemberAddRequest, spaceMember);
        vaildSpaceMember(spaceMember, true);
        // 数据库操作
        boolean result = this.save(spaceMember);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceMember.getId();
    }

    @Override
    public void vaildSpaceMember(SpaceMember spaceMember, boolean add) {
        ThrowUtill.throwIf(spaceMember == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceMember.getSpaceId();
        Long userId = spaceMember.getUserId();
        if (add) {
            // todo 添加空间成员时，成员已经在空间，就不可以添加
            // 创建时，空间 id 和用户 id 必填
            ThrowUtill.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtill.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtill.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色
        String spaceRole = spaceMember.getSpaceUserRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }

    @Override
    public SpaceMemberVo getSpaceMemberVo(SpaceMember spaceMember, HttpServletRequest request) {
        // 对象转封装类
        SpaceMemberVo spaceUserVO = SpaceMemberVo.objToVo(spaceMember);
        // 关联查询用户信息
        Long userId = spaceMember.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVo userVO = userService.getUserVo(user);
            spaceUserVO.setUserVo(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceMember.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVo spaceVO = spaceService.getSpaceVo(space, request);
            spaceUserVO.setSpaceVo(spaceVO);
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceMemberVo> getSpaceMemberVoList(List<SpaceMember> spaceMemberList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceMemberList)) {
            return Collections.emptyList();
        }
        // 对象列表 => 封装对象列表
        List<SpaceMemberVo> spaceMemberVoList = spaceMemberList.stream().map(SpaceMemberVo::objToVo).collect(Collectors.toList());
        // 1. 收集需要关联查询的用户 ID 和空间 ID
        Set<Long> userIdSet = spaceMemberList.stream().map(SpaceMember::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceMemberList.stream().map(SpaceMember::getSpaceId).collect(Collectors.toSet());
        // 2. 批量查询用户和空间
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet)
                .stream()
                .collect(Collectors.groupingBy(Space::getId));
        // 3. 填充 SpaceUserVO 的用户和空间信息
        spaceMemberVoList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            // 填充用户信息
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVO.setUserVo(userService.getUserVo(user));
            // 填充空间信息
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVO.setSpaceVo(SpaceVo.objToVo(space));
        });
        return spaceMemberVoList;
    }

    @Override
    public QueryWrapper<SpaceMember> getQueryWrapper(SpaceMemberQueryRequest spaceMemberQueryRequest) {
        QueryWrapper<SpaceMember> queryWrapper = new QueryWrapper<>();
        if (spaceMemberQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceMemberQueryRequest.getId();
        Long spaceId = spaceMemberQueryRequest.getSpaceId();
        Long userId = spaceMemberQueryRequest.getUserId();
        String spaceUserRole = spaceMemberQueryRequest.getSpaceUserRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceUserRole), "spaceUserRole", spaceUserRole);
        return queryWrapper;
    }
}




