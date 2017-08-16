package com.senierr.simple;

import android.app.Application;

import com.senierr.sehttp.SeHttp;

/**
 * @author zhouchunjie
 * @date 2017/8/16
 */

public class SessionApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        SeHttp.getInstance()
                .debug("SeHttp");                               // 开启调试
//                .debug(tag, isLogException)
//                .connectTimeout(SeHttp.DEFAULT_TIMEOUT)       // 设置超时，默认30秒
//                .readTimeout(SeHttp.DEFAULT_TIMEOUT)
//                .writeTimeout(SeHttp.DEFAULT_TIMEOUT)
//                .addInterceptor()                             // 添加应用层拦截器
//                .addNetworkInterceptor()                      // 添加网络层拦截器
//                .hostnameVerifier()                           // 设置域名匹配规则
//                .addCommonHeader("comHeader", "comValue")     // 添加全局头
//                .addCommonHeaders()
//                .addCommonUrlParam("comKey", "comValue")      // 添加全局参数
//                .addCommonUrlParams()
//                .retryCount(3);                               // 设置请求失败重连次数，默认不重连（0）
    }
}
