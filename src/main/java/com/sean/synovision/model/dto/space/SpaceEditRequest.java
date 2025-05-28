package com.sean.synovision.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/26
 */
@Data
public class SpaceEditRequest implements Serializable {
    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

}
