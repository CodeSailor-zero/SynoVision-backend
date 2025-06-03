package com.sean.synovision.model.dto.space;

import com.sean.synovision.common.PageResult;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/26
 */
@Data
public class SpaceQueryRequest extends PageResult implements Serializable {
    /**
     * 空间id
     */
    private Long id;

    /**
     * 创建空间用户id
     */
    private Long userId;

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
    private String searchText;
}
