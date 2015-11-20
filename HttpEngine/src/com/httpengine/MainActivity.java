package com.httpengine;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final static String TAG = "MainActivity";
	
//	private static final String TEST_URL = "http://www.weather.com.cn/data/list3/city.xml";
	private static final String TEST_URL = "https://certs.cac.washington.edu/CAtest/";
	
	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv = (TextView) findViewById(R.id.tv_text);
		
		HttpUtil.getInstance(this).doGet(TEST_URL, new HttpCallbackListener() {
			
			@Override
			public void onSuccess(String response) {
				Log.d(TAG, "onSuccess");
				tv.setText(response);
			}
			
			@Override
			public void onFailure(String message) {
				Log.d(TAG, "onFailure");
				tv.setText(message);
			}
		});
	}

}
