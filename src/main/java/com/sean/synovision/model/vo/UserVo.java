package com.sean.synovision.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author sean
 * @Date 2025/48/20
 */
@Data
public class UserVo {
    /**
     * id
     */

    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */

    private String userPassword;

    /**
     * 用户昵称
     */

    private String userName;

    /**
     * 用户头像
     */

    private String userAvatar;

    /**
     * 用户简介
     */

    private String userProfile;

    /**
     * 用户角色：user/admin
     */

    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

}
