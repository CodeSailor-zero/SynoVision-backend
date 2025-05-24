package com.sean.synovision.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * tag表
 * @TableName tag
 */
@TableName(value ="tag")
@Data
public class Tag {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    @TableField("tagName")
    private String tagName;

    /**
     * system[admin] / user 设置
     */
    @TableField("tagType")
    private String tagType;

    /**
     * 创建人Id
     */
    @TableField("createId")
    private Long createId;

    /**
     * 使用人ids【使用json】
     */
    @TableField("userIds")
    private String userIds;

    /**
     * 标签出现次数
     */
    @TableField("tagCount")
    private Integer tagCount;

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