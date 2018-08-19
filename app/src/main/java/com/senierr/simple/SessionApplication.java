package com.senierr.simple;

import android.app.Application;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.cookie.SPCookieJar;
import com.senierr.sehttp.https.SSLFactory;
import com.senierr.sehttp.https.UnSafeHostnameVerifier;
import com.senierr.sehttp.util.HttpLogInterceptor;

public class SessionApplication extends Application {

    private static SessionApplication application;
    private SeHttp seHttp;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        seHttp = new SeHttp.Builder()
                .debug("SeHttp", HttpLogInterceptor.LogLevel.BODY)
                .connectTimeout(10 * 1000)
                .readTimeout(10 * 1000)
                .writeTimeout(10 * 1000)
                .addCommonHeader("com_header", "com_header_value")
                .addCommonHeader("language", "English")
                .addCommonUrlParam("com_url_param", "com_url_param_value")
                .sslFactory(new SSLFactory())
                .hostnameVerifier(new UnSafeHostnameVerifier())
                .cookieJar(new SPCookieJar(this))
                .retryCount(3)
                .build();
    }

    public static SessionApplication getApplication() {
        return application;
    }

    public SeHttp getHttp() {
        return seHttp;
    }
}
