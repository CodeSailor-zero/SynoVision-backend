package com.sean.synovision.model.dto.tag;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author sean
 * @Date 2025/47/24
 */
@Data
public class TagUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * system[admin] / user 设置
     */
    private String tagType;
}
