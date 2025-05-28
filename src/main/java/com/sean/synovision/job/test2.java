package com.sean.synovision.job;

import cn.hutool.json.JSONUtil;
import com.sean.synovision.model.entity.User;

/**
 * @author sean
 * @Date 2025/05/29
 */
public class test2 {
    public static void main(String[] args) {
        String str = null;
        User user = new User();
        String t1 = JSONUtil.toJsonStr(user);
        String t2 = JSONUtil.toJsonStr(user + str);
        System.out.println("t1（无null）：" + t1);
        System.out.println("t2（有null）：" + t2);
    }
}
