package com.sean.synovision.model.dto.picture;

import com.sean.synovision.common.PageResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author sean
 * @Date 2025/44/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageResult implements Serializable {
    private Long id;
    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片简介
     */
    private String introduction;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签(JSON数组)
     */
    private List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 宽度 / 高度
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 是否审核，0 - 待审核，1 - 通过，2 - 未通过
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人id
     */
    private Long reviewId;

    /**
     * 审核时间
     */
    private Date reviewTime;
}
