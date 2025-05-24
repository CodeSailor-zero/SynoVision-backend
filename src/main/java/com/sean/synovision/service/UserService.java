package com.sean.synovision.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.synovision.model.dto.user.UserQueryRequest;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.user.LoginUserVo;
import com.sean.synovision.model.vo.user.UserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 24395
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-05-20 12:54:58
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);



    UserVo getUserVo(User user);
    List<UserVo> getUserVoList(List<User> userList);
    LoginUserVo getLoginUserVo(User user);
    User getLoginUser(HttpServletRequest request);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(HttpServletRequest request);
    boolean isAdmin(User user);
}
