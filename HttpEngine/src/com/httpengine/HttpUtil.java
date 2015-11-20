package com.httpengine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * 网络请求工具类
 * 
 * @author v-zhchu
 *
 */
public class HttpUtil {
	
	private final static String TAG = "HttpUtil";
    private final static String REQUEST_MOTHOD_POST = "POST";
    private final static String REQUEST_MOTHOD_GET = "GET";
    private final static String ENCODE_TYPE = "UTF-8";
    private final static int TIME_OUT = 8000;
	
    private Context context;
    private static HttpUtil httpUtil = null;
    
    private HttpUtil(Context context) {
    	this.context = context;
    }
    
    public static HttpUtil getInstance(Context context) {
    	if (httpUtil == null) {
    		httpUtil = new HttpUtil(context);
    	}
    	return httpUtil;
    }
    
    /**
     * 网络请求返回结果类
     */
    class HttpResult {
    	public boolean isSuccess;
    	public String resultStr;
    	
		public HttpResult(boolean isSuccess, String resultStr) {
			this.isSuccess = isSuccess;
			this.resultStr = resultStr;
		}
    }
    
    /**
     * POST 请求
     * @param urlStr
     * @param paramsMap
     * @param listener
     */
    public void doPost(final String urlStr, final Map<String, String> paramsMap, 
    		final HttpCallbackListener listener) {
    	if (!checkNet()) {
    		return;
    	}
    	new AsyncTask<Void, Void, HttpResult>() {

			@Override
			protected HttpResult doInBackground(Void... params) {
				String data = convertParams(paramsMap);
				HttpURLConnection connection = null;
		        try {
		            URL url = new URL(urlStr);
		            if (url.getProtocol().toLowerCase().equals("https")) {   
		                trustAllHosts();
		            }
		            connection = (HttpURLConnection) url.openConnection();
		            connection.setRequestMethod(REQUEST_MOTHOD_POST);
		            connection.setDoInput(true);
		            connection.setDoOutput(true);
		            connection.setUseCaches(false);
		            connection.setReadTimeout(TIME_OUT);
		            connection.setConnectTimeout(TIME_OUT);
		            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		            connection.setRequestProperty("Connection", "keep-alive");
		            connection.setRequestProperty("Response-Type", "json");
		            connection.setChunkedStreamingMode(0);
		            
		        	connection.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
			        connection.connect();
			        OutputStream os = connection.getOutputStream();
			        os.write(data.getBytes());
			        os.flush();
			        
			        if (connection.getResponseCode() == 200) {
			            InputStream in = connection.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(in));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						reader.close();
						in.close();
			            
			            String result = new String(response.toString().getBytes(), ENCODE_TYPE);
			            return new HttpResult(true, result);
			        }
				} catch (Exception e) {
					return new HttpResult(false, e.getMessage());
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
				return new HttpResult(false, "An unknown error occurred!");
			}
			
			@Override
			protected void onPostExecute(HttpResult result) {
				if (listener != null) {
					if (result.isSuccess) {
						listener.onSuccess(result.resultStr);
					} else {
						listener.onFailure(result.resultStr);
					}
				}
			}
    		
    	}.execute();
    }
	
    /**
     * GET 请求
     * @param urlStr
     * @param listener
     */
	public void doGet(final String urlStr, final HttpCallbackListener listener) {
		if (!checkNet()) {
    		return;
    	}
		new AsyncTask<Void, Void, HttpResult>() {

			@Override
			protected HttpResult doInBackground(Void... params) {
				HttpURLConnection connection = null;
		        try {
		        	URL url = new URL(urlStr);
		            if (url.getProtocol().toLowerCase().equals("https")) {   
		                trustAllHosts();
		            }
		            connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod(REQUEST_MOTHOD_GET);
					connection.setUseCaches(false);
					connection.setConnectTimeout(TIME_OUT);
					connection.setReadTimeout(TIME_OUT);
					
			        if (connection.getResponseCode() == 200) {
			            InputStream in = connection.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(in));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						reader.close();
						in.close();
			            
			            String result = new String(response.toString().getBytes(), ENCODE_TYPE);
			            return new HttpResult(true, result);
			        }
				} catch (Exception e) {
					return new HttpResult(false, e.getMessage());
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
				return new HttpResult(false, "An unknown error occurred!");
			}
			
			@Override
			protected void onPostExecute(HttpResult result) {
				if (listener != null) {
					if (result.isSuccess) {
						listener.onSuccess(result.resultStr);
					} else {
						listener.onFailure(result.resultStr);
					}
				}
			}
    		
    	}.execute();
	}
	
    /**
     * POST 请求参数拼接
     * @param paramsMap
     * @return
     */
    private String convertParams(Map<String, String> paramsMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : paramsMap.keySet()) {
            stringBuilder.append(key);
            stringBuilder.append("=");
            try {
                stringBuilder.append(URLEncoder.encode(paramsMap.get(key), ENCODE_TYPE));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            stringBuilder.append("&");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }
	
    /**
     * 检查是否有网络连接
     * @return
     */
	private boolean checkNet() {
		ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
		if(info != null){
			return true;
		}else{  
		    Toast.makeText(context, "亲，网络连了么？", Toast.LENGTH_LONG).show();  
		    return false;
		} 
	}
	
    /**  
     * 信任所有主机-对于任何证书都不做检查  
     * @throws KeyManagementException 
     * @throws NoSuchAlgorithmException 
     */  
    private static void trustAllHosts() throws KeyManagementException, NoSuchAlgorithmException {   
    	SSLContext context = SSLContext.getInstance("TLS");  
        context.init(null, new TrustManager[] { new TrustAllManager() }, null);  
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());  
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {   
            @Override  
            public boolean verify(String hostname, SSLSession session) {   
                return true;   
            }   
        });
    }   
  
}
