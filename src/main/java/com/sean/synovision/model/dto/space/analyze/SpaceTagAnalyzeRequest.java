package com.sean.synovision.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/30
 * 空间使用情况分析（根据图片标签）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceTagAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {

}
