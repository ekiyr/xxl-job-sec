package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.admin.service.XxlJobGroupService;
import com.xxl.job.core.biz.model.PagedRetData;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * job group controller
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
@PermissionLimit
public class JobGroupController {

	@Resource
	public XxlJobGroupDao xxlJobGroupDao;
	@Resource
	private XxlJobGroupService xxlJobGroupService;

	@RequestMapping
	public String index(Model model) {
		return "jobgroup/jobgroup.index";
	}

	@RequestMapping("/pageList")
	@ResponseBody
	public PagedRetData<XxlJobGroup> pageList(HttpServletRequest request,
											  @RequestParam(required = false, defaultValue = "0") int start,
											  @RequestParam(required = false, defaultValue = "10") int length,
											  String appname, String title) {
		// paged query
		return xxlJobGroupService.pageList(start,length,appname,title);
	}

	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> save(XxlJobGroup xxlJobGroup){
		return xxlJobGroupService.save(xxlJobGroup);
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(XxlJobGroup xxlJobGroup){
		return xxlJobGroupService.update(xxlJobGroup);
	}

	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id){
		return xxlJobGroupService.remove(id);
	}

	@RequestMapping("/loadById")
	@ResponseBody
	public ReturnT<XxlJobGroup> loadById(int id){
		return xxlJobGroupService.loadById(id);
	}

}
