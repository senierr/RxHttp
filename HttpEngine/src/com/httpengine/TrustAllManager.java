package com.httpengine;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * 信任所有的主机-对于任何证书不做检查
 * @author v-zhchu
 *
 */
public class TrustAllManager implements X509TrustManager {   
    public X509Certificate[] getAcceptedIssuers() {   
        // return null;   
        return new X509Certificate[] {};   
    }   
  
    @Override  
    public void checkClientTrusted(X509Certificate[] chain, String authType)   
            throws CertificateException {   
        // TODO Auto-generated method stub   
    }   
  
    @Override  
    public void checkServerTrusted(X509Certificate[] chain, String authType)   
            throws CertificateException {   
        // TODO Auto-generated method stub   
    }   
}; 
