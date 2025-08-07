package com.xxl.job.admin.controller.interceptor;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.service.LoginService;
import com.xxl.job.admin.util.LoginContextUtil;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截
 *
 * @author xuxueli 2015-12-12 18:09:04
 */
@Component
public class PermissionInterceptor implements AsyncHandlerInterceptor {

    @Resource
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;    // proceed with the next interceptor
        }
        //设置请求IP地址
        LoginContextUtil.getInstance().setClientIp(getReqIpAddr(request));
        // if need login
        boolean needLogin = true;
        boolean needAdminuser = false;
        HandlerMethod method = (HandlerMethod) handler;
        PermissionLimit permission = method.getMethodAnnotation(PermissionLimit.class);
        if (permission == null) {
            permission = method.getBeanType().getAnnotation(PermissionLimit.class);
        }
        if (permission != null) {
            needLogin = permission.limit();
            needAdminuser = permission.adminuser();
        }

        if (needLogin) {
            XxlJobUser loginUser = loginService.ifLogin(request, response);
            if (loginUser == null) {
                response.setStatus(302);
                response.setHeader("location", request.getContextPath() + "/toLogin");
                return false;
            }
            if (needAdminuser && loginUser.getRole() != 1) {
                throw new RuntimeException(I18nUtil.getString("system_permission_limit"));
            }
            LoginContextUtil.getInstance().setCurrentLoginName(loginUser.getUsername());
            request.setAttribute(LoginService.LOGIN_IDENTITY_KEY, loginUser);
            // set loginUser, with request
            setLoginUser(request, loginUser);
        }

        return true;    // proceed with the next interceptor
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //清除登陆信息
        LoginContextUtil.getInstance().clear();
    }


    // -------------------- permission tool --------------------

    /**
     * set loginUser
     *
     * @param request
     * @param loginUser
     */
    private static void setLoginUser(HttpServletRequest request, XxlJobUser loginUser) {
        request.setAttribute("loginUser", loginUser);
    }

    /**
     * get loginUser
     *
     * @param request
     * @return
     */
    public static XxlJobUser getLoginUser(HttpServletRequest request) {
        XxlJobUser loginUser = (XxlJobUser) request.getAttribute("loginUser");    // get loginUser, with request
        return loginUser;
    }

    /**
     * valid permission by JobGroup
     *
     * @param request
     * @param jobGroup
     */
    public static void validJobGroupPermission(HttpServletRequest request, int jobGroup) {
        XxlJobUser loginUser = getLoginUser(request);
        if (!loginUser.validPermission(jobGroup)) {
            throw new RuntimeException(I18nUtil.getString("system_permission_limit") + "[username=" + loginUser.getUsername() + "]");
        }
    }

    /**
     * filter XxlJobGroup by role
     *
     * @param request
     * @param jobGroupList_all
     * @return
     */
    public static List<XxlJobGroup> filterJobGroupByRole(HttpServletRequest request, List<XxlJobGroup> jobGroupList_all) {
        List<XxlJobGroup> jobGroupList = new ArrayList<>();
        if (jobGroupList_all != null && jobGroupList_all.size() > 0) {
            XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
            if (loginUser.getRole() == 1) {
                jobGroupList = jobGroupList_all;
            } else {
                List<String> groupIdStrs = new ArrayList<>();
                if (loginUser.getPermission() != null && loginUser.getPermission().trim().length() > 0) {
                    groupIdStrs = Arrays.asList(loginUser.getPermission().trim().split(","));
                }
                for (XxlJobGroup groupItem : jobGroupList_all) {
                    if (groupIdStrs.contains(String.valueOf(groupItem.getId()))) {
                        jobGroupList.add(groupItem);
                    }
                }
            }
        }
        return jobGroupList;
    }

    /**
     * 获取客户端IP
     *
     * @param request 请求对象
     * @return IP地址
     */
    public static String getReqIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (isUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
    }

    /**
     * 检测给定字符串是否为未知，多用于检测HTTP请求相关
     *
     * @param checkString 被检测的字符串
     * @return 是否未知
     */
    public static boolean isUnknown(String checkString) {
        return !StringUtils.hasText(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    public static String getMultistageReverseProxyIp(String ip) {
        if (!StringUtils.hasText(ip) && (ip.indexOf(",") <= 0)) {
            return ip;
        }
        // 多级反向代理检测
        final String[] ips = ip.trim().split(",");
        for (String subIp : ips) {
            if (StringUtils.hasText(subIp) && !"unknown".equalsIgnoreCase(subIp)) {
                ip = subIp;
                break;
            }
        }
        return ip;
    }

}
