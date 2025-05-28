package com.sean.synovision.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author sean
 * @Date 2025/05/27
 * 空间级别 ，对应空间枚举
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
    private int value;
    private String text;
    private long maxCount;
    private long maxSize;
}
