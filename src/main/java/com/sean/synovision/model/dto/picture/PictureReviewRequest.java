package com.sean.synovision.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author sean
 * @Date 2025/30/21
 */
@Data
public class PictureReviewRequest implements Serializable {
    /**
     * 图片id
     */
    private Long id;

    /**
     * 是否审核，0 - 待审核，1 - 通过，2 - 未通过
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
}
