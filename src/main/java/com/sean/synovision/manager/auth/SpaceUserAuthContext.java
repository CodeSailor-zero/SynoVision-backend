package com.sean.synovision.manager.auth;

import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.entity.SpaceMember;
import lombok.Data;

/**
 * @author sean
 * @Date 2025/06/02
 * 空间用户权限上下文
 */
@Data
public class SpaceUserAuthContext {

    /**
     * 临时参数，不同请求对应的 id 可能不同
     */
    private Long id;

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 空间用户 ID
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 空间用户信息
     */
    private SpaceMember spaceMember;
}