package com.sean.synovision.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * 图片表
 * @TableName picture
 */
@TableName(value ="picture")
@Data
public class Picture implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片地址
     */
    @TableField("url")
    private String url;

    /**
     * 图片名称
     */
    @TableField("name")
    private String name;

    /**
     * 图片简介
     */
    @TableField("introduction")
    private String introduction;

    /**
     * 图片分类
     */
    @TableField("category")
    private String category;

    /**
     * 图片标签(JSON数组)
     */
    @TableField("tags")
    private String tags;

    /**
     * 图片体积
     */
    @TableField("picSize")
    private Long picSize;

    /**
     * 图片宽度
     */
    @TableField("picWidth")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @TableField("picHeight")
    private Integer picHeight;

    /**
     * 宽度 / 高度
     */
    @TableField("picScale")
    private Double picScale;

    /**
     * 图片格式
     */
    @TableField("picFormat")
    private String picFormat;

    /**
     * 用户id
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