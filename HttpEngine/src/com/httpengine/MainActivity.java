package com.httpengine;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv = (TextView) findViewById(R.id.tv_text);
		
		HttpUtil.getInstance(this).doGet("http://www.weather.com.cn/data/list3/city.xml", new HttpCallbackListener() {
			
			@Override
			public void onSuccess(String response) {
				tv.setText(response);
			}
			
			@Override
			public void onFailure(String message) {
				tv.setText(message);
			}
		});
	}

}
