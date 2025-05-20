package com.sean.synovision.utill;


import cn.hutool.crypto.digest.DigestUtil;

/**
 * @author sean
 * @Date 2025/17/20
 */
public class MD5Utill {
    public static final String SLAT = "sean";
    /**
     * 加密密码
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        return DigestUtil.md5Hex(SLAT.charAt(0) + str + SLAT.charAt(3  ));
    }
}
