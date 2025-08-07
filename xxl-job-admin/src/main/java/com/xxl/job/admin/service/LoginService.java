package com.xxl.job.admin.service;

import com.xxl.job.admin.core.model.XxlJobCaptcha;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.CookieUtil;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.core.util.JacksonUtil;
import com.xxl.job.admin.dao.XxlJobCaptchaDao;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.util.MD5Util;
import com.xxl.job.core.util.AESUtil;
import com.xxl.job.admin.util.LoginContextUtil;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * @author xuxueli 2019-05-04 22:13:264
 */
@Configuration
public class LoginService {

    private static Logger logger = LoggerFactory.getLogger(LoginService.class);

    public static final String LOGIN_IDENTITY_KEY = "XXL_JOB_LOGIN_IDENTITY";
    private static final String TOKEN_AES_KEY = "svz4mfW2ynl49Q08sD5lOA";

    @Resource
    private XxlJobUserDao xxlJobUserDao;
    @Resource
    private XxlJobCaptchaDao xxlJobCaptchaDao;

    private String makeToken(XxlJobUser xxlJobUser){
        String tokenJson = JacksonUtil.writeValueAsString(xxlJobUser);
        String tokenOnAes = AESUtil.encrypt4Base64(tokenJson,TOKEN_AES_KEY);
        return new BigInteger(1,tokenOnAes.getBytes(StandardCharsets.UTF_8)).toString(16);
    }
    private XxlJobUser parseToken(String tokenHex){
         XxlJobUser xxlJobUser = null;
        if (tokenHex != null) {
            String tokenAes = new String(new BigInteger(tokenHex, 16).toByteArray(),StandardCharsets.UTF_8);
            String tokenJson = AESUtil.decrypt4Base64(tokenAes,TOKEN_AES_KEY);
            xxlJobUser = JacksonUtil.readValue(tokenJson, XxlJobUser.class);
        }
        return xxlJobUser;
    }


    public ReturnT<String> login(HttpServletRequest request, HttpServletResponse response, String username, String password,
                                 String codeUID,String verfyCode){

        // param
        if (username==null || username.trim().length()==0 || password==null || password.trim().length()==0){
            return new ReturnT<String>(500, I18nUtil.getString("login_param_empty"));
        }
        //校验验证码是否正确
        boolean validCode = verfiyCaptcha(codeUID,LoginContextUtil.getInstance().getClientIp(), verfyCode);
        if(!validCode){
            return new ReturnT<String>(500, I18nUtil.getString("invalid_captcha"));
        }
        // valid passowrd
        XxlJobUser xxlJobUser = xxlJobUserDao.loadByUserName(username);
        if (xxlJobUser == null) {
            return new ReturnT<String>(500, I18nUtil.getString("login_param_unvalid"));
        }
        String passwordMd5 = MD5Util.md5Twice(password);;
        if (!passwordMd5.equals(xxlJobUser.getPassword())) {
            return new ReturnT<String>(500, I18nUtil.getString("login_param_unvalid"));
        }
        LoginContextUtil.getInstance().setCurrentLoginName(xxlJobUser.getUsername());
        //设置登录IP地址及过期时间
        xxlJobUser.setLoginIp(LoginContextUtil.getInstance().getClientIp());
        long expireTime = System.currentTimeMillis()+8*60*60*1000L;
        xxlJobUser.setExpireTime(expireTime);
        //创建token
        String loginToken = makeToken(xxlJobUser);
        // 登录记录Cookie
        CookieUtil.set(response, LOGIN_IDENTITY_KEY, loginToken, false);
        return new ReturnT<String>(loginToken);
    }

    /**
     * logout
     *
     * @param request
     * @param response
     */
    public ReturnT<String> logout(HttpServletRequest request, HttpServletResponse response){
        CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
        LoginContextUtil.getInstance().clear();
        return ReturnT.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @return
     */
    public XxlJobUser ifLogin(HttpServletRequest request, HttpServletResponse response) {
        String cookieToken = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
        if (!StringUtils.hasText(cookieToken)) {
            return null;
        }
        XxlJobUser cookieUser = null;
        try {
            cookieUser = parseToken(cookieToken);
            if(cookieUser == null){
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logout(request, response);
            return null;
        }
        //校验登陆IP是否一致
        String curIp = LoginContextUtil.getInstance().getClientIp();
        String cookiedIp = cookieUser.getLoginIp();
        //登陆IP不一致，直接返回
        if(StringUtils.hasText(curIp) && StringUtils.hasText(cookieUser.getLoginIp()) && !curIp.equals(cookiedIp)){
            return null;
        }
        //校验登陆是否过期，若过期则直接失效
        if(System.currentTimeMillis() > cookieUser.getExpireTime()){
            return null;
        }
        XxlJobUser dbUser = xxlJobUserDao.loadByUserName(cookieUser.getUsername());
        if (dbUser != null) {
            //校验合法性
            if (!cookieUser.getPassword().equals(dbUser.getPassword())) {
                return null;
            }
            LoginContextUtil.getInstance().setCurrentLoginName(dbUser.getUsername());
            //娇艳成功则返回
            return dbUser;
        }
        return null;
    }

    /**
     * 保存或刷新验证码，同一个IP只存一条记录
     * @param codeUid
     * @param reqIp
     * @param code
     * @return
     */
    public int saveCaptcha(String codeUid,String reqIp,String code){
        int saveCount = 0 ;
        try{
            XxlJobCaptcha xxlJobCaptcha = null;
            if(StringUtils.hasText(reqIp)){
                xxlJobCaptcha = xxlJobCaptchaDao.getByReqIp(reqIp);
            }
            if(null == xxlJobCaptcha){
                xxlJobCaptcha=  new XxlJobCaptcha();
            }
            xxlJobCaptcha.setCodeUid(codeUid);
            xxlJobCaptcha.setReqIp(reqIp);
            xxlJobCaptcha.setCode(code);
            //验证码有效期5分钟
            xxlJobCaptcha.setExpireTime(System.currentTimeMillis()+5*60*1000L);
            if(null == xxlJobCaptcha.getId()) {
                saveCount = xxlJobCaptchaDao.save(xxlJobCaptcha);
            }else{
                saveCount = xxlJobCaptchaDao.update(xxlJobCaptcha);
            }
            logger.info("保存验证码({})结果：{}",codeUid,saveCount);
        }catch (Exception ex){
            logger.error("保存验证码失败！",ex);
        }
        return saveCount;
    }

    /**
     * 验证码校验
     * @param codeUid
     * @param reqIp
     * @param code
     * @return
     */
    public boolean verfiyCaptcha(String codeUid,String reqIp,String code){
        try{
            XxlJobCaptcha xxlJobCaptcha = xxlJobCaptchaDao.getByCodeUid(codeUid);
            if(null == xxlJobCaptcha){
                return false;
            }
            //过期的删除
            logger.info("验证码过期={}",System.currentTimeMillis()- xxlJobCaptcha.getExpireTime());
            if(System.currentTimeMillis() > xxlJobCaptcha.getExpireTime()){
                xxlJobCaptchaDao.delete(xxlJobCaptcha.getId());
                logger.info("验证码({})验证失败，验证码{}过期！",codeUid,code);
                return false;
            }
            //确保相同的请求IP
            if(StringUtils.hasText(xxlJobCaptcha.getReqIp()) && StringUtils.hasText(reqIp)
                    && !reqIp.equalsIgnoreCase(xxlJobCaptcha.getReqIp())){
                logger.info("验证码({})验证失败，请求IP不同src={},req={}",codeUid,xxlJobCaptcha.getReqIp(),reqIp);
                return false;
            }
            if(!code.equalsIgnoreCase(xxlJobCaptcha.getCode())){
                logger.info("验证码({})验证失败，验证码{}!={}",codeUid,xxlJobCaptcha.getCode(),code);
                return false;
            }
            xxlJobCaptchaDao.delete(xxlJobCaptcha.getId());
            logger.info("验证码({})验证通过",code);
            return true;
        }catch (Exception ex){
            logger.error("保存验证码失败！",ex);
            return false;
        }
    }

}
