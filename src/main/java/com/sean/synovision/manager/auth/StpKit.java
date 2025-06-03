package com.sean.synovision.manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * @author sean
 * @Date 2025/06/02
 * StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 * <a href="https://sa-token.cc/v/v1.39.0/doc.html#/up/many-account">官方使用文档</a>
 */
@Component
public class StpKit {

    public final static String SPACE_TYPE = "space";
    /**
     * 默认原生会话对象
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * Admin 会话对象，管理 Space 表所有账号的登录、权限认证
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}
