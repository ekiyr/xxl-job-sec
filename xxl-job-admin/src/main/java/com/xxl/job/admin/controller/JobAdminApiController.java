package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.service.XxlJobGroupService;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.Sm4Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 */
@Controller
@RequestMapping("/api/admin")
public class JobAdminApiController extends AbstractApiController {
    private static Logger logger = LoggerFactory.getLogger(JobAdminApiController.class);

    @Resource
    private XxlJobService xxlJobService;
    @Resource
    private XxlJobGroupService xxlJobGroupService;

    /**
     * api
     *
     * @param uri
     * @param dataCrypt
     * @return
     */
    @RequestMapping("/{appName}/jobinfo/{uri}")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<?> api(@PathVariable("uri") String uri, @PathVariable("appName") String appName,
                          @RequestBody(required = false) String dataCrypt) {
        try {


            //获取对应的Job Group
            ReturnT<XxlJobGroup> jobGroupRt =  xxlJobGroupService.getByAppName(appName);
            if(!jobGroupRt.isSuccess()){
                return jobGroupRt;
            }
            String acccessToken = XxlJobAdminConfig.getAdminConfig().getAccessToken();
            XxlJobGroup jobGroup = jobGroupRt.getContent();
            if(StringUtils.hasText(jobGroup.getAppSecret())){
                acccessToken = jobGroup.getAppSecret();
            }
            // valid
            ReturnT<String>  rest = checkAndDeData(uri,dataCrypt,acccessToken);
            if(!rest.isSuccess()){
                return rest;
            }
            //获取解谜之后的数据
            String data = rest.getContent();
            switch (uri.toLowerCase()){
                case "add":
                    XxlJobInfo jobInfo = GsonTool.fromJson(data, XxlJobInfo.class);
                    jobInfo.setJobGroup(jobGroup.getId());
                    return xxlJobService.add(jobInfo.getOpUser(),jobInfo);
                case "update":
                    XxlJobInfo upJobInfo = GsonTool.fromJson(data, XxlJobInfo.class);
                    upJobInfo.setJobGroup(jobGroup.getId());
                    return xxlJobService.update(upJobInfo.getOpUser(),upJobInfo);
                case "remove":
                    JobIdParam removeJobIdParam = GsonTool.fromJson(data, JobIdParam.class);
                    removeJobIdParam.setJobGroup(jobGroup.getId());
                    return xxlJobService.remove(removeJobIdParam.getAdminUser(),removeJobIdParam.getId());
                case "stop":
                    JobIdParam stopJobIdParam = GsonTool.fromJson(data, JobIdParam.class);
                    return xxlJobService.stop(stopJobIdParam.getAdminUser(),stopJobIdParam.getId());
                case "start":
                    JobIdParam startJobIdParam = GsonTool.fromJson(data, JobIdParam.class);
                    return xxlJobService.start(startJobIdParam.getAdminUser(),startJobIdParam.getId());
                case "trigger":
                    JobTriggerParam jobIdParam = GsonTool.fromJson(data, JobTriggerParam.class);
                    jobIdParam.setJobGroup(jobGroup.getId());
                    return xxlJobService.trigger(jobIdParam.getAdminUser(), jobIdParam.getId(), jobIdParam.getExecutorParam(), jobIdParam.getAddressList());
                case "nexttriggertime":
                    CronTriggerTimeParam triggerTimeParamParam = GsonTool.fromJson(data, CronTriggerTimeParam.class);
                    return xxlJobService.nextTriggerTime(triggerTimeParamParam.getScheduleType(), triggerTimeParamParam.getScheduleConf());
                default:
                    return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid jobinfo request, uri-mapping(" + uri + ") not found.");
            }
        } catch (Exception ex) {
            logger.error("请求异常！", ex);
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request!" + ex.getMessage());
        }
    }

}
