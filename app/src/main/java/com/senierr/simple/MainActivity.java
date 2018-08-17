package com.senierr.simple;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.senierr.permission.CheckCallback;
import com.senierr.permission.PermissionManager;
import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.callback.JsonCallback;
import com.senierr.sehttp.callback.StringCallback;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String URL_GET = "www.baidu.com";
    private static final String URL_POST = "";
    private static final String URL_UPLOAD = "";
    private static final String URL_DOWNLOAD = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";

    private SeHttp seHttp = SessionApplication.getApplication().getHttp();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cookie Api
//        HttpUrl httpUrl = HttpUrl.parse(URL_GET);
//        Cookie cookie = new Cookie.Builder().build();
//        List<Cookie> cookies = new ArrayList<>();
//
//        ClearableCookieJar cookieJar = seHttp.getCookieJar();
//
//        cookieJar.saveCookie(httpUrl, cookie);
//        cookieJar.saveCookies(httpUrl, cookies);
//        cookieJar.getAllCookie();
//        cookieJar.getCookies(httpUrl);
//        cookieJar.removeCookie(httpUrl, cookie);
//        cookieJar.removeCookies(httpUrl);
//        cookieJar.removeAllCookie();
    }

    public void onBtnClick(View view) {
        switch (view.getId()) {
            case R.id.btn_get:
                get();
                break;
            case R.id.btn_post:
                post();
                break;
            case R.id.btn_upload:
                PermissionManager.with(this)
                        .permissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        .request(new CheckCallback() {
                            @Override
                            public void onAllGranted() {
                                upload();
                            }
                        });
                break;
            case R.id.btn_download:
                PermissionManager.with(this)
                        .permissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        .request(new CheckCallback() {
                            @Override
                            public void onAllGranted() {
                                download();
                            }
                        });
                break;
        }
    }

    /** Get请求 */
    private void get() {
        seHttp.get(URL_GET)
                .tag(this)
                .addUrlParam("param_key", "param_value")
                .addHeader("header_key", "header_value")
                .execute(new JsonCallback<String>() {
                    @Override
                    public String parseJson(String responseStr) {
                        // 异步解析json数据
                        return responseStr;
                    }

                    @Override
                    public void onSuccess(String s) {
                        printLog(s);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        printLog(Log.getStackTraceString(e));
                    }
                });
    }

    /** Post请求 */
    private void post() {
        seHttp.post(URL_POST)
                .tag(this)
//                .addRequestParam("", "")
//                .addRequestParam("", File)
//                .setRequestBody4JSon(...)
//                .setRequestBody4Text(...)
//                .setRequestBody4Byte(...)
//                .setRequestBody4Xml(...)
//                .setRequestBody(...)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s) {
                        printLog(s);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        printLog(Log.getStackTraceString(e));
                    }
                });
    }

    /** 上传文件 */
    private void upload() {
        File destFile = new File(Environment.getExternalStorageDirectory(), "WeChat.exe");
        seHttp.post(URL_UPLOAD)
                .tag(this)
                .addRequestParam("file", destFile)
                .execute(new StringCallback() {
                    @Override
                    public void onUpload(int progress, long currentSize, long totalSize) {
                        printLog(progress + ":" + currentSize + "@" + totalSize);
                    }

                    @Override
                    public void onSuccess(String s) {
                        printLog(s);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        printLog(Log.getStackTraceString(e));
                    }
                });
    }

    /** 下载文件 */
    private void download() {
        File destFile = new File(Environment.getExternalStorageDirectory(), "WeChat.exe");
        seHttp.get(URL_DOWNLOAD)
                .tag(this)
                .execute(new FileCallback(destFile) {
                    @Override
                    public void onDownload(int progress, long currentSize, long totalSize) {
                        printLog(progress + ":" + currentSize + "@" + totalSize);
                    }

                    @Override
                    public void onSuccess(File file) {
                        printLog(file.getAbsolutePath());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        printLog(Log.getStackTraceString(e));
                    }
                });
    }

    private void printLog(String logStr) {
        Log.e(getClass().getSimpleName(), logStr);
    }
}
