package com.sean.synovision.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.synovision.annotation.AuthCheck;
import com.sean.synovision.common.BaseResponse;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.exception.ResultUtils;
import com.sean.synovision.model.dto.user.*;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.user.LoginUserVo;
import com.sean.synovision.model.vo.user.UserVo;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author sean
 * @Date 2025/42/20
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:8009"},allowCredentials = "true")
public class userController {

    @Resource
    private UserService userService;

    // region 业务
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtill.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long l = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(l);
    }
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        LoginUserVo loginUserVo = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVo);
    }

    @PostMapping("/login/out")
    public BaseResponse<Boolean> userLoginOut(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(UserConstant.user_login_state);
        ThrowUtill.throwIf(attribute == null, ErrorCode.OPERATION_ERROR);
        session.removeAttribute(UserConstant.user_login_state);
        return ResultUtils.success(true);
    }
    // endregion

    //region 增删改查
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtill.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        final String passwd = "12345678";
        user.setUserPassword(passwd);
        boolean result = userService.save(user);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }
    @GetMapping("/get/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@RequestParam("id") Long id) {
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtill.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVo>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVo> userVoPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVo> userVoList = userService.getUserVoList(userPage.getRecords());
        userVoPage.setRecords(userVoList);
        return ResultUtils.success(userVoPage);
    }

    @GetMapping("/get/vo/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVo> getUserVoById(@RequestParam("id") Long id) {
        BaseResponse<User> userBaseResponse = getUserById(id);
        User user = userBaseResponse.getData();
        UserVo userVo = userService.getUserVo(user);
        return ResultUtils.success(userVo);
    }
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        LoginUserVo loginUserVo = userService.getLoginUserVo(loginUser);
        return ResultUtils.success(loginUserVo);
    }
    @GetMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserById(@PathVariable("id") Long id) {
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(id);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtill.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.save(user);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }
    // endregion
}
