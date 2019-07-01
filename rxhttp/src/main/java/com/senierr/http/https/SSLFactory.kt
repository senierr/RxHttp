package com.senierr.http.https

import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * SSL认证
 *
 * @author zhouchunjie
 * @date 2017/8/25
 */
class SSLFactory {

    companion object {
        /**
         * 默认信任所有证书
         */
        private val UnSafeTrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    }

    var sSLSocketFactory: SSLSocketFactory? = null
    var trustManager: X509TrustManager? = null

    /**
     * 默认信任所有证书
     */
    constructor() {
        initSSLSocketFactory(null, null, null)
    }

    /**
     * 单向认证
     */
    constructor(trustManager: X509TrustManager) {
        initSSLSocketFactory(trustManager, null, null)
    }

    /**
     * 单向认证
     */
    constructor(vararg certificates: InputStream) {
        initSSLSocketFactory(null, null, null, *certificates)
    }

    /**
     * 双向认证
     */
    constructor(bksFile: InputStream, password: String, vararg certificates: InputStream) {
        initSSLSocketFactory(null, bksFile, password, *certificates)
    }

    /**
     * 双向认证
     */
    constructor(bksFile: InputStream, password: String, trustManager: X509TrustManager) {
        initSSLSocketFactory(trustManager, bksFile, password)
    }

    /**
     * 初始化SSLSocketFactory
     *
     * @param trustManager
     * @param bksFile
     * @param password
     * @param certificates
     */
    private fun initSSLSocketFactory(trustManager: X509TrustManager?, bksFile: InputStream?, password: String?, vararg certificates: InputStream) {
        try {
            val keyManagers = createKeyManager(bksFile, password)
            val trustManagers = createTrustManager(*certificates)
            val manager = if (trustManager != null) {
                // 自定义TrustManager
                trustManager
            } else if (trustManagers != null) {
                // 默认TrustManager
                getTrustManager(trustManagers)
            } else {
                // 不安全TrustManager
                UnSafeTrustManager
            }
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagers, arrayOf(manager), null)

            this.sSLSocketFactory = sslContext.socketFactory
            this.trustManager = manager
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createKeyManager(bksFile: InputStream?, password: String?): Array<KeyManager>? {
        try {
            if (bksFile == null || password == null) return null
            val clientKeyStore = KeyStore.getInstance("BKS")
            clientKeyStore.load(bksFile, password.toCharArray())
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(clientKeyStore, password.toCharArray())
            return kmf.keyManagers
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun createTrustManager(vararg certificates: InputStream): Array<TrustManager>? {
        if (certificates.isEmpty()) return null
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null)

            certificates.withIndex().forEach {
                val certificateAlias = (it.index + 1).toString()
                it.value.use { certStream ->
                    val cert = certificateFactory.generateCertificate(certStream)
                    keyStore.setCertificateEntry(certificateAlias, cert)
                }
            }
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)
            return tmf.trustManagers
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getTrustManager(trustManagers: Array<TrustManager>): X509TrustManager? {
        for (trustManager in trustManagers) {
            if (trustManager is X509TrustManager) {
                return trustManager
            }
        }
        return null
    }
}