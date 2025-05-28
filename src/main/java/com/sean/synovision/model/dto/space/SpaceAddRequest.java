package com.sean.synovision.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/26
 */
@Data
public class SpaceAddRequest implements Serializable {
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间等级 0-普通级，1-专业版，2-旗舰版
     */
    private Integer spaceLevel;
}
