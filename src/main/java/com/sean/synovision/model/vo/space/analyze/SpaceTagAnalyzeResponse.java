package com.sean.synovision.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/30
 * 空间资源使用分析响应类(根据图片标签)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeResponse implements Serializable {
    /**
     * 图片标签
     */
    private String tag;

    /**
     * 使用次数
     */
    private Long count;
}
