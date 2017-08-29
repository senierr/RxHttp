package com.senierr.simple;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.callback.StringCallback;
import com.senierr.sehttp.request.RequestBuilder;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_get)
    Button btnGet;
    @BindView(R.id.btn_post)
    Button btnPost;
    @BindView(R.id.btn_download)
    Button btnDownload;
    @BindView(R.id.btn_upload)
    Button btnUpload;
    @BindView(R.id.btn_301)
    Button btn301;
    @BindView(R.id.btn_https)
    Button btnHttps;

    private String fileDir = Environment.getExternalStorageDirectory() + "/Download/AA/";

    private StringCallback stringCallback = new StringCallback() {
        @Override
        public void onBefore(RequestBuilder requestBuilder) {
            requestBuilder.addHeader("onBefore", "requestBuilder");
            showLog("onBefore");
        }

        @Override
        public void onProgress(long totalSize, long currentSize, int progress) {
            showLog("onProgress: " + progress);
        }

        @Override
        public void onSuccess(String s) {
            showLog("onSuccess: " + s);
        }

        @Override
        public void onError(Exception e) {
            showLog("onError: " + e.toString());
        }

        @Override
        public void onAfter() {
            showLog("onAfter");
        }
    };

    private void showLog(String logStr) {
        Log.e("MainActivity", logStr);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /**
         * todo:
         *
         * cookie
         * HTTPS，证书
         */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SeHttp.getInstance().cancelTag(this);
    }

    @OnClick({R.id.btn_get, R.id.btn_post, R.id.btn_download, R.id.btn_upload, R.id.btn_301, R.id.btn_https})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_get:
                requestGet();
                break;
            case R.id.btn_post:
                requestPost();
                break;
            case R.id.btn_download:
                requestDownload();
                break;
            case R.id.btn_upload:
                requestUpload();
                break;
            case R.id.btn_301:
                request301();
                break;
            case R.id.btn_https:
                requestHttps();
                break;
        }
    }

    private void requestGet() {
        LinkedHashMap<String, String> urlParams = new LinkedHashMap<>();
        urlParams.put("urlParams1", "LinkedHashMap1");
        urlParams.put("urlParams2", "LinkedHashMap2");

        SeHttp.get(Urls.URL_METHOD)
                .addUrlParam("addUrlParam", "addUrlParam0")
                .addHeader("addHeader", "addHeader0")
                .addUrlParams(urlParams)
                .execute(stringCallback);
    }

    private void requestPost() {
        LinkedHashMap<String, String> urlParams = new LinkedHashMap<>();
        urlParams.put("urlParams1", "LinkedHashMap1");
        urlParams.put("urlParams2", "LinkedHashMap2");

        LinkedHashMap<String, String> requestParams = new LinkedHashMap<>();
        requestParams.put("requestParams1", "LinkedHashMap1");
        requestParams.put("requestParams2", "LinkedHashMap2");

        SeHttp.post(Urls.URL_METHOD)
                .addUrlParam("addUrlParam", "addUrlParam0")
                .addHeader("addHeader", "addHeader0")
                .addUrlParams(urlParams)
                .addRequestStringParams(requestParams)
                .execute(stringCallback);
    }

    private void requestDownload() {
        SeHttp.get(Urls.URL_DOWNLOAD)
                .execute(new FileCallback(new File(fileDir), "WeChatSetup.exe") {
                    @Override
                    public void onBefore(RequestBuilder requestBuilder) {
                        showLog("onBefore");
                    }

                    @Override
                    public boolean onDiff(Response response, File destFile) {
                        // 判断destFile是否是需要下载的文件，默认返回false
                        return super.onDiff(response, destFile);
                    }

                    @Override
                    public void onProgress(long totalSize, long currentSize, int progress) {
                        showLog("totalSize: " + totalSize + ", currentSize: " + currentSize + ", onProgress: " + progress);
                    }

                    @Override
                    public void onSuccess(File file) {
                        showLog("onSuccess: " + file.getPath());
                    }

                    @Override
                    public void onError(Exception e) {
                        showLog("onError: " + e.toString());
                    }

                    @Override
                    public void onAfter() {
                        showLog("onAfter");
                    }
                });
    }

    private void requestUpload() {
        HashMap<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "这里是需要提交的json格式数据");
        params.put("key3", "也可以使用三方工具将对象转成json字符串");
        params.put("key4", "其实你怎么高兴怎么写都行");
        JSONObject jsonObject = new JSONObject(params);

        SeHttp.post(Urls.URL_TEXT_UPLOAD)
                .addUrlParam("addUrlParam", "addUrlParam0")
                .addHeader("addHeader", "addHeader0")
//                .requestBody4Text("这里是需要提交的文本格式数据")        // 上传普通文本
//                .requestBody4JSon(jsonObject.toString())              // 上传JSON
                .addRequestParam("", new File(fileDir + "SeHttp.txt"))     // 上传文件
                .execute(stringCallback);
    }

    private void request301() {
        SeHttp.get(Urls.URL_REDIRECT)
                .addUrlParam("addUrlParam", "addUrlParam0")
                .addHeader("addHeader", "addHeader0")
                .execute(stringCallback);
    }

    private void requestHttps() {
//        String httpsStr = "https://github.com/jeasonlzy";   // CA认证
        String httpsStr = "https://kyfw.12306.cn/otn";    // 自签名
        SeHttp.get(httpsStr)
                .addUrlParam("addUrlParam", "addUrlParam0")
                .addHeader("addHeader", "addHeader0")
                .execute(stringCallback);
    }
}
