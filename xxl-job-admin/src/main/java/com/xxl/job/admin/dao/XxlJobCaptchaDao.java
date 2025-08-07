package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobCaptcha;
import com.xxl.job.admin.core.model.XxlJobUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 */
@Mapper
public interface XxlJobCaptchaDao {

	public XxlJobCaptcha getByCodeUid(@Param("codeUid") String codeUid);

	public XxlJobCaptcha getByReqIp(@Param("reqIp") String reqIp);

	public int save(XxlJobCaptcha xxlJobCaptcha);
	public int update(XxlJobCaptcha xxlJobCaptcha);

	public int delete(@Param("id") Integer id);

}
