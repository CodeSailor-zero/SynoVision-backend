package com.sean.synovision.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 空间成员表
 * @TableName spacemember
 */
@TableName(value ="spacemember")
@Data
public class SpaceMember {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间id
     */
    @TableField("spaceId")
    private Long spaceId;

    /**
     * 用户id
     */
    @TableField("userId")
    private Long userId;

    /**
     * 角色，viewer(r--)/editor(rw)/admin(rwx)
     */
    @TableField("spaceUserRole")
    private String spaceUserRole;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 编辑时间
     */
    @TableField("updateTime")
    private Date updateTime;
}