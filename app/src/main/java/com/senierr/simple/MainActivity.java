package com.senierr.simple;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.callback.StringCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    private String urlStr = "http://app.wz-tech.com:8091/k3dxapi";
//    private String urlStr = "http://192.168.2.155:8088/index";
//    private String urlStr = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";


    private String path = Environment.getExternalStorageDirectory() + "/Download/AA/aa";

    private void logSe(String logStr) {
        Log.e("SeH", logStr);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.tv_text);

        SeHttp.getInstance()
                // 开启调试
                .debug("SeHttp")
//                .debug(tag, isLogException)
                // 设置超时，默认30秒
                .setConnectTimeout(5000)
                .setReadTimeout(5000)
                .setWriteTimeout(5000)
                // 添加全局拦截器
//                .addInterceptor()
                // 设置域名匹配规则
//                .setHostnameVerifier()
                // 添加全局头
                .addCommonHeader("comHeader", "comValue")
//                .addCommonHeaders()
                // 添加全局参数
                .addCommonParam("comKey", "comValue")
//                .addCommonParams()
                // 设置超时重连次数，默认0次
                .setRetryCount(3);

        /**
         * todo:
         *
         * 缓存
         * cookie
         * HTTPS，证书
         */

        seHttpTest();

//        textView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                seHttpTest();
//            }
//        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SeHttp.getInstance().cancelTag(this);
    }

    private void seHttpTest() {
        HashMap<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "这里是需要提交的json格式数据");
        params.put("key3", "也可以使用三方工具将对象转成json字符串");
        params.put("key4", "其实你怎么高兴怎么写都行");
        final JSONObject jsonObject = new JSONObject(params);

        SeHttp.post(urlStr)
                .addUrlParam("key", "value")
//                .addParams()
                .addHeader("header", "value")
//                .addHeaders()
                .requestBody4JSon(jsonObject.toString())
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onBefore() {
                        logSe("onBefore");
                    }

                    @Override
                    public void onSuccess(String s) throws Exception {
                        logSe("onSuccess: " + s);
                    }

                    @Override
                    public void onError(Exception e) {
                        logSe("onError: " + e.toString());
                    }

                    @Override
                    public void onAfter() {
                        logSe("onAfter");
                    }
                });

//        SeHttp.get(urlStr)
//                .tag(this)
//                .execute(new FileCallback(path + "SeHttp.txt") {
//                    @Override
//                    public void onBefore() {
//                        logSe("onBefore");
//                    }
//
//                    @Override
//                    public void downloadProgress(long currentSize, long totalSize, int progress, long networkSpeed) {
//                        logSe("downloadProgress: " + progress);
//                    }
//
//                    @Override
//                    public void uploadProgress(long currentSize, long totalSize, int progress, long networkSpeed) {
//                        logSe("uploadProgress: " + progress);
//                    }
//
//                    @Override
//                    public void onSuccess(File file) throws Exception {
//                        logSe("onSuccess: " + file.getPath());
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        logSe("onError: " + e.toString());
//                    }
//
//                    @Override
//                    public void onAfter() {
//                        logSe("onAfter");
//                    }
//                });
    }


}
