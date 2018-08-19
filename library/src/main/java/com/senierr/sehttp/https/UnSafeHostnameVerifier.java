package com.senierr.sehttp.https;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 非安全主机名验证，默认所有可接受
 *
 * @author zhouchunjie
 * @date 2017/8/25
 */
public final class UnSafeHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
