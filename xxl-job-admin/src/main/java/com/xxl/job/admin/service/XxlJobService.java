package com.xxl.job.admin.service;


import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.core.biz.model.PagedRetData;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * core job action for xxl-job
 * 
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface XxlJobService {

	/**
	 * page list
	 *
	 * @param start
	 * @param length
	 * @param jobGroup
	 * @param jobDesc
	 * @param executorHandler
	 * @param author
	 * @return
	 */
	public PagedRetData<XxlJobInfo> pageList(int start, int length, int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author);

	/**
	 * add job
	 *
	 * @param jobInfo
	 * @return
	 */
	public ReturnT<String> add(XxlJobUser loginUser,XxlJobInfo jobInfo);
	public ReturnT<String> add(String username,XxlJobInfo jobInfo);

	/**
	 * update job
	 *
	 * @param jobInfo
	 * @return
	 */
	public ReturnT<String> update(XxlJobUser loginUser,XxlJobInfo jobInfo);
	public ReturnT<String> update(String username,XxlJobInfo jobInfo);
	/**
	 * remove job
	 * 	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> remove(XxlJobUser loginUser,int id);
	public ReturnT<String> remove(String username,int id);

	/**
	 * start job
	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> start(XxlJobUser loginUser,int id);
	public ReturnT<String> start(String username,int id);
	/**
	 * stop job
	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> stop(XxlJobUser loginUser,int id);
	public ReturnT<String> stop(String username,int id);
	/**
	 * trigger
	 *
	 * @param loginUser
	 * @param jobId
	 * @param executorParam
	 * @param addressList
	 * @return
	 */
	public ReturnT<String> trigger(XxlJobUser loginUser, int jobId, String executorParam, String addressList);
	public ReturnT<String> trigger(String username, int jobId, String executorParam, String addressList);

	/**
	 * dashboard info
	 *
	 * @return
	 */
	public Map<String,Object> dashboardInfo();

	/**
	 * chart info
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public ReturnT<Map<String,Object>> chartInfo(Date startDate, Date endDate);

	public ReturnT<List<String>> nextTriggerTime(String scheduleType, String scheduleConf);
}
