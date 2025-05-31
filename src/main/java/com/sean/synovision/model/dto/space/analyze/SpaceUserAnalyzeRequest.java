package com.sean.synovision.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/30
 * 通用空间查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 时间维度: day/week/month
     */
    private String timeDimension;

}
