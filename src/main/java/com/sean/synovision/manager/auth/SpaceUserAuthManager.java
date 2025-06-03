package com.sean.synovision.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sean.synovision.manager.auth.model.SpaceUserAuthConfig;
import com.sean.synovision.manager.auth.model.SpaceUserPermissionConstant;
import com.sean.synovision.manager.auth.model.SpaceUserRole;
import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.entity.SpaceMember;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.enums.SpaceRoleEnum;
import com.sean.synovision.model.enums.SpaceTypeEnum;
import com.sean.synovision.service.SpaceMemberService;
import com.sean.synovision.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author sean
 * @Date 2025/06/02
 * 空间权限管理器：项目启动初始化空间权限配置
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private UserService userService;

    @Resource
    private SpaceMemberService spaceMemberService;

    public static final SpaceUserAuthConfig spaceUserAuthConfig;
    static {
        String json = ResourceUtil.readUtf8Str("permisssionConfig/spacePermisssionConfig.json");
        spaceUserAuthConfig = JSONUtil.toBean( json, SpaceUserAuthConfig.class);
    }

    /**
     *  根据角色获取权限列表
     * @param spaceUserRole
     * @return
     */
    public List<String> getPromissionByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
             return new ArrayList<>();
        }
        SpaceUserRole role = spaceUserAuthConfig.getRoles()
                .stream()
                .filter(r -> r.getKey().equals(spaceUserRole))
                .findFirst()
                .orElse(null);
        if (role == null) {
             return new ArrayList<>();
        }
         return role.getPermissions();
    }

    /**
     * 获取权限列表
     *
     * @param space
     * @param loginUser
     * @return
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPromissionByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceMember spaceUser = spaceMemberService.lambdaQuery()
                        .eq(SpaceMember::getSpaceId, space.getId())
                        .eq(SpaceMember::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPromissionByRole(spaceUser.getSpaceUserRole());
                }
        }
        return new ArrayList<>();
    }

}
