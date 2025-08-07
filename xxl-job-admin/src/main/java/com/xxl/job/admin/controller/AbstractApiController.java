package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.Sm4Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class AbstractApiController {

    private static Logger logger = LoggerFactory.getLogger(AbstractApiController.class);
    protected ReturnT<String> checkAndDeData(String pathUri, String dataCrypt,String accessToken) {
        // valid
        if (!StringUtils.hasText(pathUri)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid  request, uri-mapping empty.");
        }
        if (!StringUtils.hasText(dataCrypt)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid data, request body is empty.");
        }
        if (!StringUtils.hasText(accessToken)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "The access token is wrong.");
        }
        try {
            //处理密文，设置前32个字符为执行时间，若执行时间与系统时间相差过大，直接放弃处理
            String dataWithTime = Sm4Util.decrypt4Base64WithCBC(dataCrypt, accessToken);
            long reqTime = Long.valueOf(dataWithTime.substring(0, 16));
            long diffTime = System.currentTimeMillis() - reqTime;
            if (diffTime > 60 * 1000L) {
                logger.error("invalid request({}), data is expired. reqTime = {},diffTime={}", pathUri, reqTime, diffTime);
                return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, data is expired.");
            }
            String data = dataWithTime.substring(16);
            return new ReturnT<String>(data);
        }catch (Exception ex){
            logger.error("invalid request({}), data is invalid .", pathUri, ex);
            return new ReturnT<String>(ReturnT.FAIL_CODE, "data is invalid.");
        }
    }


}
