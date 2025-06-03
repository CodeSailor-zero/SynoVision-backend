package com.sean.synovision.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/30
 * 空间图片大小响应体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse  implements Serializable {
    /**
     * 图片大小范围
     */
    private String sizeRanges;

    /**
     * 图片数量
     */
    private Long count;
}
