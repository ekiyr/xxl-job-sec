package com.xxl.job.core.biz.client;

import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.util.XxlJobRemotingUtil;

import java.util.List;

/**
 * admin api
 */
public class AdminJobInfoClient {

    private String addressUrl ;
    private String accessToken;
    private String appName;
    private String adminUser="";
    private int timeout = 5;

    public AdminJobInfoClient() {
    }
    public AdminJobInfoClient(String addressUrl, String accessToken,String appName) {
        //this.addressUrl = addressUrl;
        this.accessToken = accessToken;
        this.appName = appName;
        // valid
        if (!addressUrl.endsWith("/")) {
            addressUrl = addressUrl + "/";
        }
        //补充url
        this.addressUrl = addressUrl+"api/admin/"+this.appName+"/jobinfo/";
    }

    public AdminJobInfoClient(String addressUrl, String accessToken,String appName,String adminUser) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
        this.appName = appName;
        // valid
        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
        this.adminUser = adminUser;
        //补充url
        this.addressUrl = addressUrl+"api/admin/"+this.appName+"/jobinfo/";
    }

    public ReturnT<String> add(JobInfo jobInfo) {
        jobInfo.setOpUser(adminUser);
        return XxlJobRemotingUtil.postBody(addressUrl+"add", accessToken, timeout,
                jobInfo, String.class);
    }

    public ReturnT<String> update(JobInfo jobInfo) {
        jobInfo.setOpUser(adminUser);
        return XxlJobRemotingUtil.postBody(addressUrl+"update", accessToken, timeout,
                jobInfo, String.class);
    }

    public ReturnT<String> remove(int jobId) {
        JobIdParam jobIdParam = new JobIdParam();
        jobIdParam.setId(jobId);
        jobIdParam.setAdminUser(adminUser);
        return XxlJobRemotingUtil.postBody(addressUrl + "remove", accessToken, timeout, jobIdParam, String.class);
    }

    public ReturnT<String> start(int jobId) {
        JobIdParam jobIdParam = new JobIdParam();
        jobIdParam.setId(jobId);
        jobIdParam.setAdminUser(adminUser);
        return XxlJobRemotingUtil.postBody(addressUrl + "start", accessToken, timeout, jobIdParam, String.class);
    }

    public ReturnT<String> stop(int jobId) {
        JobIdParam jobIdParam = new JobIdParam();
        jobIdParam.setId(jobId);
        jobIdParam.setAdminUser(adminUser);
        return XxlJobRemotingUtil.postBody(addressUrl + "stop", accessToken, timeout, jobIdParam, String.class);
    }

    public ReturnT<String> trigger(JobTriggerParam jobTriggerParam) {
        jobTriggerParam.setAdminUser(adminUser);
        return XxlJobRemotingUtil.postBody(addressUrl + "trigger", accessToken, timeout,
                jobTriggerParam, String.class);
    }

    /**
     * 下次触发时间
     * @param triggerTimeParamParam
     * @return
     */
    public ReturnT<List<String>> nextTriggerTime(CronTriggerTimeParam triggerTimeParamParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "nexttriggertime", accessToken, timeout,
                triggerTimeParamParam);
    }


    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAddressUrl(String addressUrl) {
        this.addressUrl = addressUrl;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
