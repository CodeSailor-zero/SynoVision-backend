package com.sean.synovision.model.dto.spaceMember;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/06/02
 * 空间成员添加请求
 */
@Data
public class SpaceMemberAddRequest implements Serializable {
    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}
