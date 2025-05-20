package com.sean.synovision.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/44/20
 */
@Data
public class UserUpdateRequest implements Serializable {
    private Long id;
    private String userName;
    private String userAccount;
    private String userAvatar;
    private String userProfile;
    // user / admin
    private String userRole;
}
