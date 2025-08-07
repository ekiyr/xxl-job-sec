package com.xxl.job.core.util;

import com.google.gson.reflect.TypeToken;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 *
 */
public class XxlJobRemotingUtil {
    private static Logger logger = LoggerFactory.getLogger(XxlJobRemotingUtil.class);
    public static final String XXL_JOB_ACCESS_TOKEN = "XXL-JOB-ACCESS-TOKEN";

    /**
     * post
     *
     * @param url
     * @param accessToken
     * @param timeout
     * @param requestObj
     * @param returnTargClassOfT
     * @return
     */
    public static <T> ReturnT<T> postBody(String url, String accessToken, int timeout, Object requestObj, Class<T> returnTargClassOfT) {
        String requestBody = null;
        if (null != requestObj) {
            requestBody = GsonTool.toJson(requestObj);
        }
        ReturnT<String> resp = postBody(url, accessToken, timeout, requestBody);
        // parse returnT

        if (resp.isSuccess()) {
            String resultJson = resp.getContent();
            try {
                ReturnT<T> returnT = GsonTool.fromJson(resultJson, ReturnT.class, returnTargClassOfT);
                return returnT;
            } catch (Exception e) {
                logger.error("xxl-job remoting (url=" + url + ") response content invalid(" + resultJson + ").", e);
                return new ReturnT<T>(ReturnT.FAIL_CODE, "xxl-job remoting (url=" + url + ") response content invalid(" + resultJson + ").");
            }
        } else {
            return new ReturnT<T>(ReturnT.FAIL_CODE, resp.getMsg());
        }
    }

    /**
     * post
     *
     * @param url
     * @param accessToken
     * @param timeout
     * @param requestObj
     * @return
     */
    public static <T> ReturnT<List<T>> postBody(String url, String accessToken, int timeout, Object requestObj) {
        String requestBody = null;
        if (null != requestObj) {
            requestBody = GsonTool.toJson(requestObj);
        }
        ReturnT<String> resp = postBody(url, accessToken, timeout, requestBody);
        // parse returnT

        if (resp.isSuccess()) {
            String resultJson = resp.getContent();
            try {
                Type type = new TypeToken<ReturnT<List<T>>>() {
                }.getType();
                ReturnT<List<T>> returnT = GsonTool.getGson().fromJson(resultJson, type);
                return returnT;
            } catch (Exception e) {
                logger.error("xxl-job remoting (url=" + url + ") response content invalid(" + resultJson + ").", e);
                return new ReturnT<List<T>>(ReturnT.FAIL_CODE, "xxl-job remoting (url=" + url + ") response content invalid(" + resultJson + ").");
            }
        } else {
            return new ReturnT<List<T>>(ReturnT.FAIL_CODE, resp.getMsg());
        }
    }


    /**
     * post
     *
     * @param url
     * @param accessToken
     * @param timeout
     * @param requestString
     * @return
     */
    public static ReturnT<String> postBody(String url, String accessToken, int timeout, String requestString) {
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            // connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // trust-https
            boolean useHttps = url.startsWith("https");
            if (useHttps) {
                HttpsURLConnection https = (HttpsURLConnection) connection;
                trustAllHosts(https);
            }
            // connection setting
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");
            connection.connect();
            // write requestBody
            if (StringUtils.hasText(requestString)) {
                String requestBodyWithTime = String.format("%016d", System.currentTimeMillis()) + requestString;
                //执行请求加密
                String requestBodyCrypt = Sm4Util.encrypt4Base64WithCBC(requestBodyWithTime, accessToken);
                logger.debug("Request({}) with requestBodyCrypt:{}", url, requestBodyCrypt);
                try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());) {
                    dataOutputStream.write(requestBodyCrypt.getBytes("UTF-8"));
                    dataOutputStream.flush();
                    dataOutputStream.close();
                }
            }
            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                return new ReturnT<String>(ReturnT.FAIL_CODE,
                        "xxl-job remoting fail, StatusCode(" + statusCode + ") invalid. for url : " + url);
            }
            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            //直接返回
            String resultJson = result.toString();
            return new ReturnT<String>(resultJson);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ReturnT<String>(ReturnT.FAIL_CODE, "xxl-job remoting error(" + e.getMessage() + "), for url : " + url);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
        }
    }


    // trust-https start
    private static void trustAllHosts(HttpsURLConnection connection) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();

            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }};


}
