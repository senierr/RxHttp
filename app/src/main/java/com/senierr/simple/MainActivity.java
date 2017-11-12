package com.senierr.simple;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.callback.FileCallback1;
import com.senierr.sehttp.callback.JsonCallback;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "https://www.baidu.com";
    public static final String URL_DOWNLOAD = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";

    @BindView(R.id.btn_request)
    Button btnRequest;
    @BindView(R.id.btn_pause1)
    Button btnPause1;
    @BindView(R.id.btn_download1)
    Button btnDownload1;
    @BindView(R.id.pb_progress1)
    ProgressBar pbProgress1;
    @BindView(R.id.btn_pause2)
    Button btnPause2;
    @BindView(R.id.btn_download2)
    Button btnDownload2;
    @BindView(R.id.pb_progress2)
    ProgressBar pbProgress2;
    @BindView(R.id.btn_pause3)
    Button btnPause3;
    @BindView(R.id.btn_download3)
    Button btnDownload3;
    @BindView(R.id.pb_progress3)
    ProgressBar pbProgress3;
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

    @OnClick({R.id.btn_request, R.id.btn_pause1, R.id.btn_download1, R.id.btn_pause2, R.id.btn_download2, R.id.btn_pause3, R.id.btn_download3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request:
                request();
                break;
            case R.id.btn_pause1:
                SeHttp.getInstance().cancelAll();
                startPoint = _currentSize;
                break;
            case R.id.btn_download1:
                download();
                break;
            case R.id.btn_pause2:
                break;
            case R.id.btn_download2:
                break;
            case R.id.btn_pause3:
                break;
            case R.id.btn_download3:
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
                    public String parseJSon(String responseStr) throws Exception {
                        // 解析JSon
                        return responseStr;
                    }

                    @Override
                    public void onSuccess(String s) {
                        tvText.setText(s);
                    }

                    @Override
                    public void onError(Exception e) {
                        tvText.setText(Log.getStackTraceString(e));
                    }
                });
    }

    long _totalSize;
    long _currentSize;
    long startPoint;

    /**
     * 下载
     */
    private void download() {
        SeHttp.get(URL_DOWNLOAD)
                .tag(this)
                .addHeader("RANGE", "bytes=" + startPoint + "-")
                .execute(new FileCallback1(new File(fileDir), "WeChatSetup.exe", startPoint) {
                    @Override
                    public boolean onDiff(Response response, File destFile) {
                        // 判断destFile是否是需要下载的文件，默认返回false
                        return super.onDiff(response, destFile);
                    }

                    @Override
                    public void onDownloadProgress(long totalSize, long currentSize, int progress) {
                        tvText.setText("totalSize: " + totalSize + "\n currentSize: " + currentSize + "\n onProgress: " + progress);
                        if (_currentSize == 0) {
                            _totalSize = totalSize;
                            pbProgress1.setMax((int) _totalSize);
                        }

                        if (_totalSize == totalSize) {
                            _currentSize = currentSize;
                        } else {
                            _currentSize = startPoint + currentSize;
                        }

                        pbProgress1.setProgress((int) _currentSize);
                        Log.e("download", _totalSize + ": " + startPoint + ", " + _currentSize);
                    }

                    @Override
                    public void onSuccess(File file) {
                        tvText.setText("onSuccess: " + file.getPath() + ", " + file.length());
                    }

                    @Override
                    public void onError(Exception e) {
                        tvText.setText(Log.getStackTraceString(e));
                    }
                });
    }
}
