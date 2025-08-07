package com.xxl.job.admin.util;

import java.util.HashMap;
import java.util.Map;

public class LoginContextUtil extends ThreadLocal<Map<String, Object>> {

    @Override
    protected Map<String, Object> initialValue() {
        return new HashMap<>(15);
    }

    private static final LoginContextUtil instance = new LoginContextUtil();

    private LoginContextUtil() {
    }

    public static LoginContextUtil getInstance() {
        return instance;
    }

    public void clear() {
        this.get().clear();
    }

    public Object get(String key) {
        return this.get().get(key);
    }

    public void set(String key, Object value) {
        this.get().put(key, value);
    }

    public Map<String, Object> getAll() {
        return this.get();
    }

    public void set(Map<String, Object> map) {
        this.get().putAll(map);
    }

    public void setRunEnv(String area) {
        this.set("env", area);
    }

    public String getRunEnv() {
        Object obj = this.get("env");
        return obj != null ? (String)obj : "";
    }

    public void setCurrentUserId(String currentUserId) {
        this.set("currentUserId", currentUserId);
    }

    public String getCurrentUserId() {
        Object obj = this.get("currentUserId");
        return obj != null ? (String)obj : "0";
    }

    public void setCurrentLoginName(String currentLoginName) {
        this.set("currentLoginName", currentLoginName);
    }

    public String getCurrentLoginName() {
        Object obj = this.get("currentLoginName");
        return obj != null ? (String)obj : "";
    }
    public void setVersion(String version) {
        this.set("version", version);
    }

    public String getVersion() {
        Object obj = this.get("version");
        return obj != null ? (String)obj : "";
    }

    public void setCurrentUrl(String currentUrl) {
        this.set("currentUrl", currentUrl);
    }

    public String getCurrentUrl() {
        Object obj = this.get("currentUrl");
        return obj != null ? (String)obj : "";
    }

    public void setClientIp(String clientIp) {
        this.set("clientIp", clientIp);
    }

    public String getClientIp() {
        Object obj = this.get("clientIp");
        return obj != null ? (String)obj : "";
    }

}
