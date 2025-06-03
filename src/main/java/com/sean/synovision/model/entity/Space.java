package com.sean.synovision.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间表
 * @TableName space
 */
@TableName(value ="space")
@Data
public class Space implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    @TableField("spaceName")
    private String spaceName;

    /**
     * 空间等级 0-普通级，1-专业版，2-旗舰版
     */
    @TableField("spaceLevel")
    private Integer spaceLevel;

    /**
     * 空间等级 0-私有，1-团队
     */
    @TableField("spaceType")
    private Integer spaceType;

    /**
     * 空间图片最大容量
     */
    @TableField("maxSize")
    private Long maxSize;

    /**
     * 空间图片最大数量
     */
    @TableField("maxCount")
    private Long maxCount;

    /**
     * 空间图片总容量
     */
    @TableField("totalSize")
    private Long totalSize;

    /**
     * 当前空间下图片总数量
     */
    @TableField("totalCount")
    private Long totalCount;

    /**
     * 创建用户id
     */
    @TableField("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 编辑时间
     */
    @TableField("editTime")
    private Date editTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("isDelete")
    private Integer isDelete;
}