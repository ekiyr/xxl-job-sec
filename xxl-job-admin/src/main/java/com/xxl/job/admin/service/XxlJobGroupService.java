package com.xxl.job.admin.service;


import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.core.biz.model.PagedRetData;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.util.AESUtil;
import com.xxl.job.core.util.Sm4Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * core job group action for xxl-job
 */
@Service
public class XxlJobGroupService {

	private static final Logger log = LoggerFactory.getLogger(XxlJobGroupService.class);
	@Resource
	public XxlJobInfoDao xxlJobInfoDao;
	@Resource
	public XxlJobGroupDao xxlJobGroupDao;

	@Resource
	private XxlJobRegistryDao xxlJobRegistryDao;

	public PagedRetData<XxlJobGroup> pageList(int start, int length,  String appName, String title) {
		// page query
		List<XxlJobGroup> list = xxlJobGroupDao.pageList(start, length, appName, title);
		int list_count = xxlJobGroupDao.pageListCount(start, length, appName, title);
		for(XxlJobGroup item: list){
			item.setAppSecret("");
		}
		PagedRetData <XxlJobGroup> pagedRetData = new PagedRetData<XxlJobGroup>(list);;
		pagedRetData.setRecordsTotal(list_count);
		pagedRetData.setRecordsFiltered(list_count);
		return pagedRetData;
	}

	public ReturnT<String> save(XxlJobGroup xxlJobGroup){

		// valid
		if (xxlJobGroup.getAppname()==null || xxlJobGroup.getAppname().trim().length()==0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input")+"AppName") );
		}
		if (xxlJobGroup.getAppname().length()<4 || xxlJobGroup.getAppname().length()>64) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appname_length") );
		}
		if (xxlJobGroup.getAppname().contains(">") || xxlJobGroup.getAppname().contains("<")) {
			return new ReturnT<String>(500, "AppName"+I18nUtil.getString("system_unvalid") );
		}
		if (xxlJobGroup.getTitle()==null || xxlJobGroup.getTitle().trim().length()==0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")) );
		}
		if (xxlJobGroup.getTitle().contains(">") || xxlJobGroup.getTitle().contains("<")) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_title")+I18nUtil.getString("system_unvalid") );
		}
		if (xxlJobGroup.getAddressType()!=0) {
			if (xxlJobGroup.getAddressList()==null || xxlJobGroup.getAddressList().trim().length()==0) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit") );
			}
			if (xxlJobGroup.getAddressList().contains(">") || xxlJobGroup.getAddressList().contains("<")) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList")+I18nUtil.getString("system_unvalid") );
			}

			String[] addresss = xxlJobGroup.getAddressList().split(",");
			for (String item: addresss) {
				if (item==null || item.trim().length()==0) {
					return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid") );
				}
			}
		}
		//校验appname 是否存在
		XxlJobGroup oldJobGroup = xxlJobGroupDao.getByAppName(xxlJobGroup.getAppname());
		if(null != oldJobGroup) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appname_duplicate"));
		}
		// 设置更新时间
		xxlJobGroup.setUpdateTime(new Date());
		//自动生成连接密钥
		xxlJobGroup.setAppSecret(Sm4Util.genAesKey4Base64());
		JOB_GROUP_CACHE.clear();
		try{
			int ret = xxlJobGroupDao.save(xxlJobGroup);
			return (ret>0)?new ReturnT(xxlJobGroup.getAppSecret()):ReturnT.FAIL;
		}catch (Exception exception){
			log.error("XXLJobGroup保存失败",exception);
			if(exception.getMessage().contains("Duplicate entry")){
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appname_duplicate")+"-"+xxlJobGroup.getAppname());
			}
			return ReturnT.FAIL;
		}
	}


	public ReturnT<String> update(XxlJobGroup xxlJobGroup){
		// valid
		if (!StringUtils.hasText(xxlJobGroup.getAppname())) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input")+"AppName") );
		}
		if (xxlJobGroup.getAppname().length()<4 || xxlJobGroup.getAppname().length()>64) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appname_length") );
		}
		if (xxlJobGroup.getTitle()==null || xxlJobGroup.getTitle().trim().length()==0) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")) );
		}
		if (xxlJobGroup.getAddressType() == 0) {
			// 0=自动注册
			List<String> registryList = findRegistryByAppName(xxlJobGroup.getAppname());
			String addressListStr = null;
			if (registryList!=null && !registryList.isEmpty()) {
				Collections.sort(registryList);
				addressListStr = "";
				for (String item:registryList) {
					addressListStr += item + ",";
				}
				addressListStr = addressListStr.substring(0, addressListStr.length()-1);
			}
			xxlJobGroup.setAddressList(addressListStr);
		} else {
			// 1=手动录入
			if (xxlJobGroup.getAddressList()==null || xxlJobGroup.getAddressList().trim().length()==0) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit") );
			}
			String[] addresss = xxlJobGroup.getAddressList().split(",");
			for (String item: addresss) {
				if (item==null || item.trim().length()==0) {
					return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid") );
				}
			}
		}

		// process
		xxlJobGroup.setUpdateTime(new Date());

		int ret = xxlJobGroupDao.update(xxlJobGroup);
		JOB_GROUP_CACHE.clear();
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}


	private List<String> findRegistryByAppName(String appnameParam){
		HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
		List<XxlJobRegistry> list = xxlJobRegistryDao.findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
		if (list != null) {
			for (XxlJobRegistry item: list) {
				if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
					String appname = item.getRegistryKey();
					List<String> registryList = appAddressMap.get(appname);
					if (registryList == null) {
						registryList = new ArrayList<String>();
					}

					if (!registryList.contains(item.getRegistryValue())) {
						registryList.add(item.getRegistryValue());
					}
					appAddressMap.put(appname, registryList);
				}
			}
		}
		return appAddressMap.get(appnameParam);
	}

	public ReturnT<String> remove(int id){

		// valid
		int count = xxlJobInfoDao.pageListCount(0, 10, id, -1,  null, null, null);
		if (count > 0) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_del_limit_0") );
		}

		List<XxlJobGroup> allList = xxlJobGroupDao.findAll();
		if (allList.size() == 1) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_del_limit_1") );
		}
		JOB_GROUP_CACHE.clear();
		int ret = xxlJobGroupDao.remove(id);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	public ReturnT<XxlJobGroup> loadById(int id){
		XxlJobGroup jobGroup = xxlJobGroupDao.load(id);
		if(null != jobGroup){
			jobGroup.setAppSecret("");
		}
		return jobGroup!=null?new ReturnT<XxlJobGroup>(jobGroup):new ReturnT<XxlJobGroup>(ReturnT.FAIL_CODE, null);
	}

	//缓存JOB GROUP
	private static final Map<String,XxlJobGroup> JOB_GROUP_CACHE = new ConcurrentHashMap<String,XxlJobGroup>();
	//获取保存的job group
	public ReturnT<XxlJobGroup> getByAppName(String appName) {
		if(!StringUtils.hasText(appName)){
			return new ReturnT<XxlJobGroup>(ReturnT.FAIL_CODE, appName+ "appname is empty!");
		}
		XxlJobGroup jobGroup = JOB_GROUP_CACHE.get(appName);
		if(null == jobGroup){
			jobGroup = xxlJobGroupDao.getByAppName(appName);
			if(null != jobGroup){
				JOB_GROUP_CACHE.put(appName,jobGroup);
			}
		}
		return jobGroup!=null?new ReturnT<XxlJobGroup>(jobGroup):new ReturnT<XxlJobGroup>(ReturnT.FAIL_CODE, appName+ "is invalid!");
	}

}
