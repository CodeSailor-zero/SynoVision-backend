package com.sean.synovision.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/11/20
 */
@Data
public class PageResult implements Serializable {
    private int current = 1;
    private int pageSize = 10;
    private String sortField;
    private String sortOrder = "desc";
}
