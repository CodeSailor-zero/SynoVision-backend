package com.sean.synovision.model.dto.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/30
 * 通用空间查询请求
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {
    /**
     * 空间ID
     */
    private Long spaceId;
    /**
     * 是否查询公开空间
     */
    private boolean queryPublic;
    /**
     * 是否查询所有空间
     */
    private boolean queryAll;
}
