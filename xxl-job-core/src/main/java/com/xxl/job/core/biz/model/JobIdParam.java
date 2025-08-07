package com.xxl.job.core.biz.model;

import java.io.Serializable;

public class JobIdParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private int id;				// 主键ID

    private int jobGroup;

    private String adminUser;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(int jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }
}
