package com.sean.synovision.model.vo.spaceMember;

import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.entity.SpaceMember;
import com.sean.synovision.model.vo.space.SpaceVo;
import com.sean.synovision.model.vo.user.UserVo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * @author sean
 * @Date 2025/06/02
 */
@Data
public class SpaceMemberVo {
    /**
     * id
     */
    private Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceUserRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date updateTime;

    /**
     * 创建人信息
     */
    private UserVo userVo;

    /**
     * 团队空间信息
     */
    private SpaceVo spaceVo;

    public static SpaceMemberVo objToVo(SpaceMember spaceMember) {
        if (spaceMember == null) {
            return null;
        }
        SpaceMemberVo spaceMemberVo = new SpaceMemberVo();
        BeanUtils.copyProperties(spaceMember, spaceMemberVo);
        return spaceMemberVo;
    }

    public static SpaceMember voToObj(SpaceMemberVo spaceMemberVo) {
        if (spaceMemberVo == null) {
            return null;
        }
        SpaceMember spaceMember = new SpaceMember();
        BeanUtils.copyProperties(spaceMemberVo, spaceMember);
        return spaceMember;
    }
}
