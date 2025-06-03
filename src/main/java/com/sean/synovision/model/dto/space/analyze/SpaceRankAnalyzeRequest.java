package com.sean.synovision.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/30
 * 管理员对空间使用排行
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {
    /**
     * 空间id
     */
    private Long spaceId;
    /**
     * 空间排行展示数
     */
    private Integer topNum = 10;
}
