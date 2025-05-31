package com.sean.synovision.model.vo.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/30
 * 空间资源使用分析响应类
 */
@Data
public class SpaceUsageAnalyzeResponse implements Serializable {
    /**
     * 已使用大小
     */
    private Long usedSize;

    /**
     *总大下
     */
    private Long maxSize;

    /**
     * 空间使用比例
     */
    private Double sizeUsageRation;

    /**
     * 当前空间的图片数量
     */
    private Long usedCount;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 图片使用比例
     */
    private Double countUsageRation;
}
