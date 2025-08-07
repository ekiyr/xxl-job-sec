package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.service.XxlJobGroupService;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.Sm4Util;
import com.xxl.job.core.util.XxlJobRemotingUtil;
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
 * Created by xuxueli on 17/5/10.
 */
@Controller
@RequestMapping("/api")
public class JobApiController {
    private static Logger logger = LoggerFactory.getLogger(JobApiController.class);

    @Resource
    private AdminBiz adminBiz;

    @Resource
    private XxlJobGroupService xxlJobGroupService;

    /**
     * api
     *
     * @param uri
     * @param dataCrypt
     * @return
     */
    @RequestMapping("/{uri}")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> api(HttpServletRequest request, @PathVariable("uri") String uri,
                               @RequestBody(required = false) String dataCrypt) {
        return apiOnAppName(request,null, uri, dataCrypt);
    }

    /**
     * api
     *
     * @param uri
     * @param dataCrypt
     * @return
     */
    @RequestMapping("/{appName}/{uri}")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> apiOnAppName(HttpServletRequest request,
                               @PathVariable("appName") String appName,
                               @PathVariable("uri") String uri,
                               @RequestBody(required = false) String dataCrypt) {

        // valid
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, HttpMethod not support.");
        }
        if (uri == null || uri.trim().length() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping empty.");
        }
        if (!StringUtils.hasText(XxlJobAdminConfig.getAdminConfig().getAccessToken())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "The access token is wrong.");
        }
        try {
            String accessToken = XxlJobAdminConfig.getAdminConfig().getAccessToken();
            if(StringUtils.hasText(appName)){
                ReturnT<XxlJobGroup> jobGroupRt =  xxlJobGroupService.getByAppName(appName);
                if(jobGroupRt.isSuccess()){
                    XxlJobGroup jobGroup = jobGroupRt.getContent();
                    if(StringUtils.hasText(jobGroup.getAppSecret())){
                        accessToken = jobGroup.getAppSecret();
                    }
                }
            }
            //处理密文，设置前32个字符为执行时间，若执行时间与系统时间相差过大，直接放弃处理
            String dataWithTime = Sm4Util.decrypt4Base64WithCBC(dataCrypt, accessToken);
            Long reqTime = Long.valueOf(dataWithTime.substring(0, 16));
            long diffTime = System.currentTimeMillis() - reqTime;
            if (diffTime > 60 * 1000L) {
                logger.error("invalid request({}), data is expired. reqTime = {},diffTime={}",uri,reqTime,diffTime);
                return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, data is expired.");
            }
            String data = dataWithTime.substring(16);
            // services mapping
            if ("callback".equals(uri)) {
                List<HandleCallbackParam> callbackParamList = GsonTool.fromJson(data, List.class, HandleCallbackParam.class);
                return adminBiz.callback(callbackParamList);
            } else if ("registry".equals(uri)) {
                RegistryParam registryParam = GsonTool.fromJson(data, RegistryParam.class);
                return adminBiz.registry(registryParam);
            } else if ("registryRemove".equals(uri)) {
                RegistryParam registryParam = GsonTool.fromJson(data, RegistryParam.class);
                return adminBiz.registryRemove(registryParam);
            }
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping(" + uri + ") not found.");
        } catch (Exception ex) {
            logger.error("请求异常！", ex);
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request!" + ex.getMessage());
        }
    }

}
