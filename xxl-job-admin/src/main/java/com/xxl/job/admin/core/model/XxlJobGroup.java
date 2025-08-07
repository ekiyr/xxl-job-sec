package com.xxl.job.admin.core.model;

import com.xxl.job.core.biz.model.JobGroup;

/**
 * Created by xuxueli on 16/9/30.
 */
public class XxlJobGroup extends JobGroup {

    private String appSecret;

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
}
