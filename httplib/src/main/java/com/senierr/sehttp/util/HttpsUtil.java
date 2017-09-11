package com.senierr.sehttp.util;

import com.senierr.sehttp.entity.SSLParams;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.internal.Util;

/**
 * HTTPS工具类
 *
 * @author zhouchunjie
 * @date 2017/8/25
 */

public class HttpsUtil {

    /**
     * 默认信任所有证书
     *
     * @return
     */
    public static SSLParams getSslSocketFactory() {
        return getSslSocketFactoryBase(null, null, null);
    }

    /**
     * 单向认证
     *
     * @param trustManager
     * @return
     */
    public static SSLParams getSslSocketFactory(X509TrustManager trustManager) {
        return getSslSocketFactoryBase(trustManager, null, null);
    }

    /**
     * 单向认证
     *
     * @param certificates
     * @return
     */
    public static SSLParams getSslSocketFactory(InputStream... certificates) {
        return getSslSocketFactoryBase(null, null, null, certificates);
    }

    /**
     * 双向认证
     *
     * @param bksFile
     * @param password
     * @param certificates
     * @return
     */
    public static SSLParams getSslSocketFactory(InputStream bksFile, String password, InputStream... certificates) {
        return getSslSocketFactoryBase(null, bksFile, password, certificates);
    }

    /**
     * 双向认证
     *
     * @param bksFile
     * @param password
     * @param trustManager
     * @return
     */
    public static SSLParams getSslSocketFactory(InputStream bksFile, String password, X509TrustManager trustManager) {
        return getSslSocketFactoryBase(trustManager, bksFile, password);
    }

    private static SSLParams getSslSocketFactoryBase(X509TrustManager trustManager, InputStream bksFile, String password, InputStream... certificates) {
        try {
            KeyManager[] keyManagers = createKeyManager(bksFile, password);
            TrustManager[] trustManagers = createTrustManager(certificates);
            X509TrustManager manager;
            if (trustManager != null) {
                // 自定义TrustManager
                manager = trustManager;
            } else if (trustManagers != null) {
                // 默认TrustManager
                manager = getTrustManager(trustManagers);
            } else {
                // 不安全TrustManager
                manager = UnSafeTrustManager;
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, new TrustManager[]{manager}, null);

            SSLParams sslParams = new SSLParams();
            sslParams.setsSLSocketFactory(sslContext.getSocketFactory());
            sslParams.setTrustManager(manager);

            return sslParams;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static KeyManager[] createKeyManager(InputStream bksFile, String password) {
        try {
            if (bksFile == null || password == null) return null;
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksFile, password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientKeyStore, password.toCharArray());
            return kmf.getKeyManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static TrustManager[] createTrustManager(InputStream... certificates) {
        if (certificates == null || certificates.length <= 0) return null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certStream : certificates) {
                String certificateAlias = Integer.toString(index++);
                Certificate cert = certificateFactory.generateCertificate(certStream);
                keyStore.setCertificateEntry(certificateAlias, cert);
                Util.closeQuietly(certStream);
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            return tmf.getTrustManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static X509TrustManager getTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    /**
     * 默认信任所有证书
     */
    public static X509TrustManager UnSafeTrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    };
}
