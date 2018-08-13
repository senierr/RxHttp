package com.senierr.sehttp.https;

import com.senierr.sehttp.util.HttpsUtil;

import java.io.InputStream;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * SSL认证
 *
 * @author zhouchunjie
 * @date 2017/8/25
 */

public class SSLFactory {

    private SSLSocketFactory sSLSocketFactory;
    private X509TrustManager trustManager;

    /**
     * 默认信任所有证书
     *
     * @return
     */
    public static SSLFactory create() {
        return HttpsUtil.getSslSocketFactoryBase(null, null, null);
    }

    /**
     * 单向认证
     *
     * @param trustManager
     * @return
     */
    public static SSLFactory create(X509TrustManager trustManager) {
        return HttpsUtil.getSslSocketFactoryBase(trustManager, null, null);
    }

    /**
     * 单向认证
     *
     * @param certificates
     * @return
     */
    public static SSLFactory create(InputStream... certificates) {
        return HttpsUtil.getSslSocketFactoryBase(null, null, null, certificates);
    }

    /**
     * 双向认证
     *
     * @param bksFile
     * @param password
     * @param certificates
     * @return
     */
    public static SSLFactory create(InputStream bksFile, String password, InputStream... certificates) {
        return HttpsUtil.getSslSocketFactoryBase(null, bksFile, password, certificates);
    }

    /**
     * 双向认证
     *
     * @param bksFile
     * @param password
     * @param trustManager
     * @return
     */
    public static SSLFactory create(InputStream bksFile, String password, X509TrustManager trustManager) {
        return HttpsUtil.getSslSocketFactoryBase(trustManager, bksFile, password);
    }

    public SSLSocketFactory getsSLSocketFactory() {
        return sSLSocketFactory;
    }

    public void setsSLSocketFactory(SSLSocketFactory sSLSocketFactory) {
        this.sSLSocketFactory = sSLSocketFactory;
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    public void setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
    }
}
