package com.sean.synovision.model.dto.tag;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/40/24
 */
@Data
public class TagAddRequest implements Serializable {
    private String tagName;
    private String tagType;
}
