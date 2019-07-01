package com.senierr.http.https

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * 非安全主机名验证，默认所有可接受
 *
 * @author zhouchunjie
 * @date 2017/8/25
 */
class UnSafeHostnameVerifier : HostnameVerifier {

    override fun verify(hostname: String, session: SSLSession): Boolean {
        return true
    }
}
