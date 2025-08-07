package com.xxl.job.core.biz.model;

import java.io.Serializable;

public class CronTriggerTimeParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private String scheduleType;
    private String scheduleConf	;

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getScheduleConf() {
        return scheduleConf;
    }

    public void setScheduleConf(String scheduleConf) {
        this.scheduleConf = scheduleConf;
    }
}
