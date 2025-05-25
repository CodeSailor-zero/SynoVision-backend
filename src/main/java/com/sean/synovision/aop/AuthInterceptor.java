package com.sean.synovision.aop;

import com.sean.synovision.annotation.AuthCheck;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.enums.UserRoleEnum;
import com.sean.synovision.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author sean
 * @Date 2025/24/20
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {//切点，通过注解来实现什么时候执行。
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        //将需要权限转为枚举
        UserRoleEnum needRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //1.接口不需要权限
        if (needRoleEnum == null) {
            return joinPoint.proceed();
        }
        //2.接口需要权限，检查用户是否具备相应权限
        String userRole = loginUser.getUserRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
        //2.1 用户没有权限
        if (userRoleEnum == null || !userRoleEnum.equals(needRoleEnum)) {
            throw new BussinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //2.2 用户具备权限，执行接口
        return joinPoint.proceed();
    }
}
