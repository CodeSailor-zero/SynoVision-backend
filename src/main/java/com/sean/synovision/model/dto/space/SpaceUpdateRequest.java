package com.sean.synovision.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/26
 */
@Data
public class SpaceUpdateRequest implements Serializable {
    /**
     * 空间id
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
     * 空间图片最大容量
     */
    private Long maxSize;

    /**
     * 空间图片最大数量
     */
    private Long maxCount;


}
