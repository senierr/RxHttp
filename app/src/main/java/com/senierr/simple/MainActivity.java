package com.senierr.simple;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.cache.CacheConfig;
import com.senierr.sehttp.cache.CacheMode;
import com.senierr.sehttp.callback.StringCallback;
import com.senierr.sehttp.util.FileUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import okhttp3.Cache;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button button;

    private String urlStr = "http://app.wz-tech.com:8091/k3dxapi";
//    private String urlStr = "http://192.168.2.155:8088/index";
//    private String urlStr = "http://www.da.com";
//    private String urlStr = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";


    private String path = Environment.getExternalStorageDirectory() + "/Download/AA/";


    private static final long cacheSize = 1024*1024*20;//缓存文件最大限制大小20M
    private static String cachedirectory = Environment.getExternalStorageDirectory() + "/Download/AA/";  //设置缓存文件路径
    private static Cache cache = new Cache(new File(cachedirectory), cacheSize);  //

    private void logSe(String logStr) {
        Log.e("SeH", logStr);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.tv_text);
        button = (Button) findViewById(R.id.btn_start);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seHttpTest();
            }
        });

        CacheConfig cacheConfig = CacheConfig.build()
                .cacheFile(FileUtil.getCacheDirectory(this, null))  // 设置缓存路径，默认在应用缓存目录
                .cacheTime(1000 * 3600 * 24 * 7)                            // 设置缓存有效时长
                .maxSize(1024 * 1024 * 10);                                 // 设置缓存大小

        SeHttp.init(getApplication())
                .debug("SeHttp")                              // 开启调试
//                .debug(tag, isLogException)
//                .connectTimeout(SeHttp.DEFAULT_TIMEOUT)       // 设置超时，默认30秒
//                .readTimeout(SeHttp.DEFAULT_TIMEOUT)
//                .writeTimeout(SeHttp.DEFAULT_TIMEOUT)
//                .addInterceptor()                             // 添加全局拦截器
//                .hostnameVerifier()                           // 设置域名匹配规则
//                .addCommonHeader("comHeader", "comValue")     // 添加全局头
//                .addCommonHeaders()
//                .addCommonUrlParam("comKey", "comValue")      // 添加全局参数
//                .addCommonUrlParams()
                .cacheConfig(cacheConfig)                                // 设置缓存参数
                .retryCount(3);                                 // 设置请求失败重连次数，默认不重连（0）


        /**
         * todo:
         *
         * 缓存
         * cookie
         * HTTPS，证书
         */
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

        SeHttp.post(urlStr)                                      // 请求方式及URL
                .tag(this)                                      // 设置标签，用于取消请求
//                .addUrlParam("key", "value")                  // 添加单个URL参数
//                .addUrlParams()                               // 添加多个URL参数
//                .addHeader("header", "value")                 // 添加单个请求头
//                .addHeaders()                                 // 添加多个请求头
//                .requestBody4Text()                           // 设置文本格式请求体
//                .requestBody4JSon(jsonObject.toString())      // 设置JSON格式请求体
//                .requestBody4Xml()                            // 设置XML格式请求体
//                .requestBody4Byte()                           // 设置字节流格式请求提
//                .requestBody()                                // 设置自定义请求体
                .addRequestParam("key", "param")              // 添加单个请求体键值对（字符串）
//                .addRequestParam("key", new File())           // 添加单个请求体键值对（文件）
//                .addRequestStringParams()                     // 添加多个请求体键值对（字符串）
//                .addRequestFileParams()                       // 添加多个请求体键值对（文件）
//                .build()                                      // 生成OkHttp请求
//                .cacheKey(urlStr)                             // 设置缓存key
//                .cacheMode(CacheMode.CACHE_THEN_REQUEST)      // 设置缓存模式，默认NO_CACHE
//                .cacheTime(1000 * 10)                         // 设置缓存有效时长
//                .execute()                                    // 同步请求
                .execute(new StringCallback() {                 // 异步请求
                    @Override
                    public void onBefore() {
                        logSe("onBefore");
                    }

                    @Override
                    public void onSuccess(String s, boolean isCache) throws Exception {
                        logSe(isCache+ ", onSuccess: " + s);
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
//                    public boolean isDiff(Response response, File destFile) {
//                        // 判断destFile是否是需要下载的文件，默认返回false
//                        return super.isDiff(response, destFile);
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
