package com.senierr.sehttp.entity;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * HTTPS参数
 *
 * @author zhouchunjie
 * @date 2017/8/25
 */

public class SSLParams {
    private SSLSocketFactory sSLSocketFactory;
    private X509TrustManager trustManager;

    public SSLSocketFactory getsSLSocketFactory() {
        return sSLSocketFactory;
    }

    public SSLParams setsSLSocketFactory(SSLSocketFactory sSLSocketFactory) {
        this.sSLSocketFactory = sSLSocketFactory;
        return this;
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    public SSLParams setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }
}
