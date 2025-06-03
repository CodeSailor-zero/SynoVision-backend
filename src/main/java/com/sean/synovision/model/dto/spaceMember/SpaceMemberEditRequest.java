package com.sean.synovision.model.dto.spaceMember;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/06/02
 * 空间成员编辑请求
 */
@Data
public class SpaceMemberEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceUserRole;
}
