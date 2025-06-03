package com.sean.synovision.model.vo.space;

import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.vo.user.UserVo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sean
 * @Date 2025/32/21
 */
@Data
public class SpaceVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级 0-普通级，1-专业版，2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间等级 0-私有，1-团队
     */
    private Integer spaceType;

    /**
     * 空间图片最大容量
     */
    private Integer maxSize;

    /**
     * 空间图片最大数量
     */
    private Integer maxCount;

    /**
     * 空间图片总容量
     */
    private Integer totalSize;

    /**
     * 当前空间下图片总数量
     */
    private Integer totalCount;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建空间用户信息
     */
    private UserVo userVo;

    /**
     *  权限列表
     */
    private List<String> parmissionList = new ArrayList<>();

    public static SpaceVo objToVo(Space space){
        if (space == null){
            return null;
        }
        SpaceVo spaceVo = new SpaceVo();
        BeanUtils.copyProperties(space, spaceVo);
        return spaceVo;
    }

    public static Space voToObj(SpaceVo spaceVo){
        if (spaceVo == null){
            return null;
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceVo,space);
        return space;
    }
}
