package com.sean.synovision.manager.auth.annotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.sean.synovision.manager.auth.StpKit;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 空间权限认证：必须具有指定权限才能进入该方法
 * <p> 可标注在函数、类上（效果等同于标注在此类的所有方法上）
 * <a href="https://sa-token.cc/v/v1.39.0/doc.html#/up/many-account?id=_7%e3%80%81%e4%bd%bf%e7%94%a8%e6%b3%a8%e8%a7%a3%e5%90%88%e5%b9%b6%e7%ae%80%e5%8c%96%e4%bb%a3%e7%a0%81"/>
 */
@SaCheckPermission(type = StpKit.SPACE_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SaSpaceCheckPermission {

    /**
     * 需要校验的权限码
     *
     * @return 需要校验的权限码
     */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] value() default {};

    /**
     * 验证模式：AND | OR，默认AND
     *
     * @return 验证模式
     */
    @AliasFor(annotation = SaCheckPermission.class)
    SaMode mode() default SaMode.AND;

    /**
     * 在权限校验不通过时的次要选择，两者只要其一校验成功即可通过校验
     *
     * <p>
     * 例1：@SaCheckPermission(value="user-add", orRole="admin")，
     * 代表本次请求只要具有 user-add权限 或 admin角色 其一即可通过校验。
     * </p>
     *
     * <p>
     * 例2： orRole = {"admin", "manager", "staff"}，具有三个角色其一即可。 <br>
     * 例3： orRole = {"admin, manager, staff"}，必须三个角色同时具备。
     * </p>
     *
     * @return /
     */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] orRole() default {};

}