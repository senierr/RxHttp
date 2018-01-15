package com.senierr.simple;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.callback.JsonCallback;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "https://www.baidu.com";
    public static final String URL_DOWNLOAD = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";

    @BindView(R.id.tv_text)
    TextView tvText;

    private String fileDir = Environment.getExternalStorageDirectory() + "/Download/AA/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SeHttp.getInstance().cancelTag(this);
    }

    @OnClick({R.id.btn_request, R.id.btn_download})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request:
                request();
                break;
            case R.id.btn_download:
                download();
                break;
        }
    }

    /**
     * 普通请求
     */
    private void request() {
        SeHttp
                .get(URL)                           // 请求：post/put/delete/head/options/patch
                .tag(this)                          // 标签：关闭请求时使用
//                .addUrlParam("key", "value")      // 添加URL参数
//                .addUrlParams(...)
//                .addHeader("key", "value")        // 添加头
//                .addHeaders(...)
//                .addRequestParam("key", "value")  // 添加请求体参数
//                .addRequestStringParams(...)
//                .addRequestParam("key", ...)
//                .addRequestFileParams(...)
//                .requestBody4Text(...)            // 设置请求体：文本
//                .requestBody4JSon(...)            // 设置请求体：JSon
//                .requestBody4Xml(...)             // 设置请求体：XML
//                .requestBody4Byte(...)            // 设置请求体：字节流
//                .requestBody(...)                 // 设置自定义请求体
//                .build(...)                       // 生成OkHttp请求
//                .execute()                        // 同步请求
//                .execute(new StringCallback() {   // 异步String回调
//                    @Override
//                    public void onSuccess(String s) {
//                    }
//                })
                .execute(new JsonCallback<String>() {
                    @Override
                    public String parseJson(String responseStr) throws Exception {
                        // 解析JSon
                        return responseStr;
                    }

                    @Override
                    public void onSuccess(String s) {
                        tvText.setText("onSuccess:\n" + s);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        tvText.setText("onFailure:\n" + Log.getStackTraceString(e));
                    }
                });
    }

    /**
     * 下载
     */
    private void download() {
        SeHttp.get(URL_DOWNLOAD)
                .tag(this)
                .execute(new FileCallback(new File(fileDir), "WeChatSetup.exe") {
                    @Override
                    public boolean onDiff(Response response, File destFile) {
                        // 判断destFile是否是需要下载的文件，默认返回false
                        return super.onDiff(response, destFile);
                    }

                    @Override
                    public void onDownloadProgress(long totalSize, long currentSize, int progress) {
                        tvText.setText("totalSize: " + totalSize + "\n" +
                                "currentSize: " + currentSize + "\n" +
                                "onProgress: " + progress + "%");
                    }

                    @Override
                    public void onSuccess(File file) {
                        tvText.setText("onSuccess: " + file.getPath() + ", " + file.length());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        tvText.setText(Log.getStackTraceString(e));
                    }
                });
    }
}
