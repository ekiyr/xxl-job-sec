package com.xxl.job.admin.core.model;

/**
 */
public class XxlJobCaptcha {

    private Integer id;
    private String codeUid;        // 验证码编码
    private String reqIp;        // 请求IP
    private String code;        // 验证码
    private long expireTime;    //到期时间

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodeUid() {
        return codeUid;
    }

    public void setCodeUid(String codeUid) {
        this.codeUid = codeUid;
    }

    public String getReqIp() {
        return reqIp;
    }

    public void setReqIp(String reqIp) {
        this.reqIp = reqIp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
}
