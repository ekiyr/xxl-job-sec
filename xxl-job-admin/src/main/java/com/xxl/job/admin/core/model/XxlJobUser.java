package com.xxl.job.admin.core.model;

import org.springframework.util.StringUtils;

/**
 * @author xuxueli 2019-05-04 16:43:12
 */
public class XxlJobUser {
	
	private int id;
	private String username;		// 账号
	private String password;		// 密码
	private int role;				// 角色：0-普通用户、1-管理员
	private String permission;	// 权限：执行器ID列表，多个逗号分割
	private String loginIp;     //不存库，登陆IP，Token使用
	private long expireTime;    //不存库，到期时间

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	// plugin
	public boolean validPermission(int jobGroup){
		if (this.role == 1) {
			return true;
		} else {
			if (StringUtils.hasText(this.permission)) {
				for (String permissionItem : this.permission.split(",")) {
					if (String.valueOf(jobGroup).equals(permissionItem)) {
						return true;
					}
				}
			}
			return false;
		}

	}

}
