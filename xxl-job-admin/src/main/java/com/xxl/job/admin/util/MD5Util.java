package com.xxl.job.admin.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class MD5Util {

    public static String md5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }
    /** 两次MD5 */
    public static String md5Twice(String text) {
        return  md5(md5(text));
    }

}
