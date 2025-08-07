package com.xxl.job.admin.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.math.Calculator;
import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.service.LoginService;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.admin.util.LoginContextUtil;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
public class IndexController {

	private static Logger logger = LoggerFactory.getLogger(IndexController.class);

	@Resource
	private XxlJobService xxlJobService;
	@Resource
	private LoginService loginService;


	@RequestMapping("/")
	public String index(Model model) {

		Map<String, Object> dashboardMap = xxlJobService.dashboardInfo();
		model.addAllAttributes(dashboardMap);

		return "index";
	}

    @RequestMapping("/chartInfo")
	@ResponseBody
	@PermissionLimit
	public ReturnT<Map<String, Object>> chartInfo(Date startDate, Date endDate) {
        ReturnT<Map<String, Object>> chartInfo = xxlJobService.chartInfo(startDate, endDate);
        return chartInfo;
    }
	
	@RequestMapping("/toLogin")
	@PermissionLimit(limit=false)
	public ModelAndView toLogin(HttpServletRequest request, HttpServletResponse response,ModelAndView modelAndView) {
		if (loginService.ifLogin(request, response) != null) {
			modelAndView.setView(new RedirectView("/",true,false));
			return modelAndView;
		}
		modelAndView.getModel().putAll(getCapcha());
		modelAndView.setViewName("login");
		return modelAndView;
	}


	@RequestMapping("/getCapcha")
	@PermissionLimit(limit=false)
	@ResponseBody
	public Map<String,String> getCapcha() {
		LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(140,32,new MathGenerator(1),16);
		lineCaptcha.createCode();
		lineCaptcha.createCode();
		lineCaptcha.getCode(); //存储当前的code；
		String imageBase64 = lineCaptcha.getImageBase64Data();
		String pageUUID = UUID.randomUUID().toString().toUpperCase();
		String codeUID = new BigInteger(pageUUID.getBytes()).toString(16).toUpperCase();
		Map<String,String> returnMap = new HashMap<>();
		returnMap.put("codeUID",codeUID);
		returnMap.put("imgBase64",imageBase64);
		int code= (int)Calculator.conversion(lineCaptcha.getCode());
		loginService.saveCaptcha(codeUID, LoginContextUtil.getInstance().getClientIp(), code+"");
		logger.info("获取验证码，codeUID：{},验证码为:{} {}",codeUID,lineCaptcha.getCode(), code);
		return returnMap;
	}

	@RequestMapping(value="login", method=RequestMethod.POST)
	@ResponseBody
	@PermissionLimit(limit=false)
	public ReturnT<String> loginDo(HttpServletRequest request, HttpServletResponse response, String userName, String password,
								   String codeUID,String verfyCode){
		return loginService.login(request, response, userName, password, codeUID,verfyCode);
	}
	
	@RequestMapping(value="logout", method=RequestMethod.POST)
	@ResponseBody
	@PermissionLimit(limit=false)
	public ReturnT<String> logout(HttpServletRequest request, HttpServletResponse response){
		return loginService.logout(request, response);
	}
	
	@RequestMapping("/help")
	public String help() {

		/*if (!PermissionInterceptor.ifLogin(request)) {
			return "redirect:/toLogin";
		}*/

		return "help";
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
}
