package com.xxl.job.admin.util;

import com.xxl.job.admin.core.util.I18nUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.util.Base64;
import java.util.UUID;

/**
 * email util test
 *
 * @author xuxueli 2017-12-22 17:16:23
 */
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class I18nUtilTest {
    private static Logger logger = LoggerFactory.getLogger(I18nUtilTest.class);

    @Test
    public void test(){
        logger.info(I18nUtil.getString("admin_name"));
        logger.info(I18nUtil.getMultString("admin_name", "admin_name_full"));
        logger.info(I18nUtil.getMultString());
    }


    @Test
    public void  testUID(){
        String pageUID = UUID.randomUUID().toString();
        System.out.println(Base64.getEncoder().encodeToString(pageUID.getBytes()));
        String pageUIDHex = new BigInteger(pageUID.getBytes()).toString(16).toUpperCase();
        System.out.println(pageUIDHex);
    }
}
