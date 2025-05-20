package com.sean.synovision.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/20
 */
@Data
public class UserRegisterRequest implements Serializable {
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
