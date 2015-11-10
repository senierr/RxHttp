package com.httpengine;

/**
 * 网络请求回调接口
 * @author v-zhchu
 *
 */
public interface HttpCallbackListener {
	
	void onSuccess(String response);
	
	void onFailure(String message);
}
