package com.senierr.simple;

import android.app.Application;

import com.senierr.http.RxHttp;
import com.senierr.http.cookie.SPCookieJar;
import com.senierr.http.https.SSLFactory;
import com.senierr.http.https.UnSafeHostnameVerifier;
import com.senierr.http.internal.LogInterceptor;

public class SessionApplication extends Application {

    private static SessionApplication application;
    private RxHttp rxHttp;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        rxHttp = new RxHttp.Builder()
                .debug("SeHttp", LogInterceptor.LogLevel.BODY)
                .connectTimeout(10 * 1000)
                .readTimeout(10 * 1000)
                .writeTimeout(10 * 1000)
                .addCommonHeader("com_header", "com_header_value")
                .addCommonHeader("language", "English")
                .addCommonUrlParam("com_url_param", "com_url_param_value")
                .sslFactory(new SSLFactory())
                .hostnameVerifier(new UnSafeHostnameVerifier())
                .cookieJar(new SPCookieJar(this))
                .build();
    }

    public static SessionApplication getApplication() {
        return application;
    }

    public RxHttp getHttp() {
        return rxHttp;
    }
}
