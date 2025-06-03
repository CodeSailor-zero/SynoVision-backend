package com.sean.synovision.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author sean
 * @Date 2025/06/02
 * spacePermisssionConfig.json 配置的 映射类
 */
@Data
public class SpaceUserAuthConfig implements Serializable {
    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;
}
