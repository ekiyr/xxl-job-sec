package com.xxl.job.core.biz.model;

import java.io.Serializable;

public class JobTriggerParam extends JobIdParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private String executorParam;
    private String addressList;

    public String getExecutorParam() {
        return executorParam;
    }

    public void setExecutorParam(String executorParam) {
        this.executorParam = executorParam;
    }

    public String getAddressList() {
        return addressList;
    }

    public void setAddressList(String addressList) {
        this.addressList = addressList;
    }
}
