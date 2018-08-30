package com.senierr.simple;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.FileConverter;
import com.senierr.http.converter.StringConverter;
import com.senierr.http.internal.Progress;
import com.senierr.http.internal.Result;
import com.senierr.permission.CheckCallback;
import com.senierr.permission.PermissionManager;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String HOST = "https://project.mzlion.com/easy-okhttp/api";

    private static final String URL_GET = HOST + "/ip-info";
    private static final String URL_POST = HOST + "/post/simple";
    private static final String URL_UPLOAD = HOST + "/post/form";
    private static final String URL_DOWNLOAD = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";

    private RxHttp rxHttp = SessionApplication.getApplication().getHttp();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
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
            case R.id.btn_cancel:
                compositeDisposable.clear();
                break;
        }
    }

    private Consumer<Throwable> throwableConsumer = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) throws Exception {
            printLog("--onFailure: " + Log.getStackTraceString(throwable));
        }
    };

    /** Get请求 */
    private void get() {
        Disposable disposable = rxHttp.get(URL_GET)
                .addUrlParam("ip", "112.64.217.29")
                .addHeader("language", "China")
                .execute(new MyConverter())
                .singleOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Result<MyEntity>>() {
                    @Override
                    public void accept(Result<MyEntity> myEntityResult) throws Exception {
                        MyEntity body = myEntityResult.body();
                        if (body != null) {
                            printLog("--onSuccess: " + body.toString());
                        }
                    }
                }, throwableConsumer);
        compositeDisposable.add(disposable);
    }

    /** Post请求 */
    private void post() {
        Disposable disposable = rxHttp.post(URL_POST)
                .addRequestParam("name", "hello")
                .addRequestParam("age", "18")
                .execute(new StringConverter())
                .singleOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Result<String>>() {
                    @Override
                    public void accept(Result<String> stringResult) throws Exception {
                        String body = stringResult.body();
                        if (body != null) {
                            printLog("--onSuccess: " + body);
                        }
                    }
                }, throwableConsumer);
        compositeDisposable.add(disposable);
    }

    /** 上传文件 */
    private void upload() {
        File destFile = new File(Environment.getExternalStorageDirectory(), "111.png");
        Disposable disposable = rxHttp.post(URL_UPLOAD)
                .addRequestParam("file", destFile)
                .openUploadListener(true)
                .execute(new StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Result<String>>() {
                    @Override
                    public void accept(Result<String> stringResult) throws Exception {
                        Progress progress = stringResult.uploadProgress();
                        if (progress != null) {
                            printLog("--onProgress: " + progress.toString());
                        }

                        String body = stringResult.body();
                        if (body != null) {
                            printLog("--onSuccess: " + body);
                        }
                    }
                }, throwableConsumer);
        compositeDisposable.add(disposable);
    }

    /** 下载文件 */
    private void download() {
        File destFile = new File(Environment.getExternalStorageDirectory(), "WeChat.exe");
        Disposable disposable = rxHttp.get(URL_DOWNLOAD)
                .openDownloadListener(true)
                .execute(new FileConverter(destFile))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Result<File>>() {
                    @Override
                    public void accept(Result<File> stringResult) throws Exception {
                        Progress progress = stringResult.downloadProgress();
                        if (progress != null) {
                            printLog("--onProgress: " + progress.toString());
                        }

                        File body = stringResult.body();
                        if (body != null) {
                            printLog("--onSuccess: " + body.getPath());
                        }
                    }
                }, throwableConsumer);
        compositeDisposable.add(disposable);
    }

    private void printLog(String logStr) {
        Log.e(getClass().getSimpleName(), logStr);
    }
}
