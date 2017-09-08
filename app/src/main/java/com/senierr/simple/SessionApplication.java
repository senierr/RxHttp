package com.senierr.simple;

import android.app.Application;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.interceptor.HttpLogInterceptor;
import com.senierr.sehttp.interceptor.LogLevel;
import com.senierr.sehttp.util.HttpsUtil;

import okio.Buffer;

/**
 * @author zhouchunjie
 * @date 2017/8/16
 */

public class SessionApplication extends Application {

    private static final String CER_12306 = "-----BEGIN CERTIFICATE-----\n" +                                                  //
            "MIICmjCCAgOgAwIBAgIIbyZr5/jKH6QwDQYJKoZIhvcNAQEFBQAwRzELMAkGA1UEBhMCQ04xKTAn\n" + //
            "BgNVBAoTIFNpbm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMB4X\n" + //
            "DTA5MDUyNTA2NTYwMFoXDTI5MDUyMDA2NTYwMFowRzELMAkGA1UEBhMCQ04xKTAnBgNVBAoTIFNp\n" + //
            "bm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMIGfMA0GCSqGSIb3\n" + //
            "DQEBAQUAA4GNADCBiQKBgQDMpbNeb34p0GvLkZ6t72/OOba4mX2K/eZRWFfnuk8e5jKDH+9BgCb2\n" + //
            "9bSotqPqTbxXWPxIOz8EjyUO3bfR5pQ8ovNTOlks2rS5BdMhoi4sUjCKi5ELiqtyww/XgY5iFqv6\n" + //
            "D4Pw9QvOUcdRVSbPWo1DwMmH75It6pk/rARIFHEjWwIDAQABo4GOMIGLMB8GA1UdIwQYMBaAFHle\n" + //
            "tne34lKDQ+3HUYhMY4UsAENYMAwGA1UdEwQFMAMBAf8wLgYDVR0fBCcwJTAjoCGgH4YdaHR0cDov\n" + //
            "LzE5Mi4xNjguOS4xNDkvY3JsMS5jcmwwCwYDVR0PBAQDAgH+MB0GA1UdDgQWBBR5XrZ3t+JSg0Pt\n" + //
            "x1GITGOFLABDWDANBgkqhkiG9w0BAQUFAAOBgQDGrAm2U/of1LbOnG2bnnQtgcVaBXiVJF8LKPaV\n" + //
            "23XQ96HU8xfgSZMJS6U00WHAI7zp0q208RSUft9wDq9ee///VOhzR6Tebg9QfyPSohkBrhXQenvQ\n" + //
            "og555S+C3eJAAVeNCTeMS3N/M5hzBRJAoffn3qoYdAO1Q8bTguOi+2849A==\n" +                 //
            "-----END CERTIFICATE-----";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        SeHttp.getInstance()
                .debug("SeHttp", LogLevel.BODY)                 // 开启调试
//                .connectTimeout(SeHttp.DEFAULT_TIMEOUT)       // 设置超时，默认30秒
//                .readTimeout(SeHttp.DEFAULT_TIMEOUT)
//                .writeTimeout(SeHttp.DEFAULT_TIMEOUT)
//                .addInterceptor()                             // 添加应用层拦截器
//                .addNetworkInterceptor()                      // 添加网络层拦截器
//                .hostnameVerifier()                           // 设置域名匹配规则
                .sslSocketFactory(HttpsUtil.getSslSocketFactory(
                        new Buffer().writeUtf8(CER_12306).inputStream()))
                .addCommonHeader("comHeader", "comValue")     // 添加全局头
//                .addCommonHeaders()
                .addCommonUrlParam("comKey", "comValue")      // 添加全局参数
//                .addCommonUrlParams()
                .retryCount(2);                               // 设置请求失败重连次数，默认不重连（0）
    }
}
