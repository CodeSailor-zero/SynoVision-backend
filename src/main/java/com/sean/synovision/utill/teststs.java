package com.sean.synovision.utill;

/**
 * @author sean
 * @Date 2025/05/31
 */
public class teststs {
    public static void main(String[] args) {
        String u = "https://synovision-1328821647.cos.ap-nanjing.myqcloud.com//public/1924703832596598786/2025-05-30_b3d10be3-5de1-4a58-aef5-7982dd911bdf.webp";
        int i = u.indexOf("public");
        System.out.println(i);
        String substring = u.substring(i);
        System.out.println(substring);
    }
}
