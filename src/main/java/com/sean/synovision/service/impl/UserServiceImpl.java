package com.sean.synovision.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.mapper.UserMapper;
import com.sean.synovision.model.dto.user.UserQueryRequest;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.enums.UserRoleEnum;
import com.sean.synovision.model.vo.LoginUserVo;
import com.sean.synovision.model.vo.UserVo;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.MD5Utill;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 24395
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-05-20 12:54:58
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"参数异常");
        }
        if (userAccount.length() < 4) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户账号长度不能小于4");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        Long count = this.baseMapper.selectCount(userQueryWrapper);
        if (count > 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户账号已存在");
        }
        // 加密 [ salt[0] + passwod + salt[3] ]
        String md5Passwd = MD5Utill.getMD5(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(md5Passwd);
        user.setUserName("默认名字");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean result = this.save(user);
        if (!result) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"用户注册失败");
        }
        return user.getId();
    }

    @Override
    public LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"参数异常");
        }
        if (userAccount.length() < 4) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户账号长度不能小于4");
        }
        if (userPassword.length() < 8) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }

        String md5Passwd = MD5Utill.getMD5(userPassword);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", md5Passwd);
        User user = this.baseMapper.selectOne(userQueryWrapper);
        ThrowUtill.throwIf(user == null, ErrorCode.PARAMS_ERROR,"用户账号或密码错误");
        request.getSession().setAttribute(UserConstant.user_login_state, user);
        return this.getLoginUserVo(user);
    }

    @Override
    public UserVo getUserVo(User user) {
        if (user == null) {
            return null;
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    @Override
    public List<UserVo> getUserVoList(List<User> userList) {
        return userList
                .stream()
                .map(this::getUserVo)
                .collect(Collectors.toList());
    }

    /**
     * 将 user 转 LoginUserVo，脱敏
     * @param user
     * @return
     */
    @Override
    public LoginUserVo getLoginUserVo(User user) {
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtils.copyProperties(user, loginUserVo);
        return loginUserVo;
    }

    /**
     * 获取当前用户
     * @param request
     * @return
     */

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.user_login_state);
        ThrowUtill.throwIf(user == null || user.getId() < 0, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        //缓存没有，从数据库查
        user = this.getById(user.getId());
        ThrowUtill.throwIf(user == null , ErrorCode.NOT_LOGIN_ERROR, "");
        return user;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"参数异常");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        userQueryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        userQueryWrapper.eq(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        userQueryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        userQueryWrapper.eq(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        userQueryWrapper.orderBy(StrUtil.isNotEmpty(sortOrder), sortOrder.equals("ascend"),sortField);
        return userQueryWrapper;
    }
}




