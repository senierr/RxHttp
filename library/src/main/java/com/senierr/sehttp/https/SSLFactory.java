package com.senierr.sehttp.https;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.internal.Util;

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
     */
    public SSLFactory() {
        initSSLSocketFactory(null, null, null);
    }

    /**
     * 单向认证
     *
     * @param trustManager
     * @return
     */
    public SSLFactory(X509TrustManager trustManager) {
        initSSLSocketFactory(trustManager, null, null);
    }

    /**
     * 单向认证
     *
     * @param certificates
     * @return
     */
    public SSLFactory(InputStream... certificates) {
        initSSLSocketFactory(null, null, null, certificates);
    }

    /**
     * 双向认证
     *
     * @param bksFile
     * @param password
     * @param certificates
     * @return
     */
    public SSLFactory(InputStream bksFile, String password, InputStream... certificates) {
        initSSLSocketFactory(null, bksFile, password, certificates);
    }

    /**
     * 双向认证
     *
     * @param bksFile
     * @param password
     * @param trustManager
     * @return
     */
    public SSLFactory(InputStream bksFile, String password, X509TrustManager trustManager) {
        initSSLSocketFactory(trustManager, bksFile, password);
    }

    /**
     * 初始化SSLSocketFactory
     *
     * @param trustManager
     * @param bksFile
     * @param password
     * @param certificates
     */
    private void initSSLSocketFactory(X509TrustManager trustManager, InputStream bksFile, String password, InputStream... certificates) {
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

            this.sSLSocketFactory = sslContext.getSocketFactory();
            this.trustManager = manager;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private KeyManager[] createKeyManager(InputStream bksFile, String password) {
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

    private TrustManager[] createTrustManager(InputStream... certificates) {
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

    private X509TrustManager getTrustManager(TrustManager[] trustManagers) {
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
    private static X509TrustManager UnSafeTrustManager = new X509TrustManager() {
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
