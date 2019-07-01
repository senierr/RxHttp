package com.senierr.simple

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.senierr.http.RxHttp
import com.senierr.http.converter.FileConverter
import com.senierr.http.converter.StringConverter
import com.senierr.http.cookie.CookieJarImpl
import com.senierr.http.cookie.store.SPCookieStore
import com.senierr.http.interceptor.LogInterceptor
import com.senierr.http.operator.ProgressProcessor
import com.senierr.permission.PermissionManager
import com.senierr.permission.RequestCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

/**
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class MainActivity : AppCompatActivity() {

    private fun Disposable.bindToActivity() {
        compositeDisposable.add(this)
    }

    companion object {
        private const val DEBUG_TAG = "Repository"
        private const val TIMEOUT = 15 * 1000L
    }

    private val compositeDisposable = CompositeDisposable()

    lateinit var rxHttp: RxHttp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        btn_get.setOnClickListener { getNormal() }
        btn_get_ignore_base_url_params.setOnClickListener { ignoreBaseUrlParams() }
        btn_get_ignore_base_headers.setOnClickListener { ignoreBaseHeaders() }
        btn_get_ignore_base_url.setOnClickListener { ignoreBaseUrl() }
        btn_post_form.setOnClickListener { postNormal() }
        btn_post_json.setOnClickListener { postJson() }
        btn_post_text.setOnClickListener { postText() }
        btn_upload.setOnClickListener { upload() }
        btn_download.setOnClickListener {
            PermissionManager.with(this)
                    .permissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .request(object : RequestCallback() {
                        override fun onAllGranted() {
                            download()
                        }
                    })
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
        Log.e(DEBUG_TAG, "onDestroy")
    }

    private fun init() {
        rxHttp = RxHttp.Builder()
                .debug(DEBUG_TAG, LogInterceptor.LogLevel.BODY)
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .writeTimeout(TIMEOUT)
                .baseUrl("https://api.test.cn")
                .addBaseHeader("header_base", "header_base")
                .addBaseUrlParam("param_base", "param_base_value")
                .cookieJar(CookieJarImpl(SPCookieStore(this)))
                .addInterceptor(MockInterceptor())
                .build()
    }

    /**
     * 普通GET请求
     */
    private fun getNormal() {
        rxHttp.get("/getInfo")
                .addUrlParam("name", "tom")
                .toResultObservable(StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
                .bindToActivity()
    }

    /**
     * 忽略基础请求参数
     */
    private fun ignoreBaseUrlParams() {
        rxHttp.get("/getInfo")
                .ignoreBaseUrlParams()
                .addUrlParam("name", "tom")
                .toResultObservable(StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
                .bindToActivity()
    }

    /**
     * 忽略基础请求头
     */
    private fun ignoreBaseHeaders() {
        rxHttp.get("/getInfo")
                .ignoreBaseHeaders()
                .addUrlParam("name", "tom")
                .toResultObservable(StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
                .bindToActivity()
    }

    /**
     * 忽略基础请求地址
     */
    private fun ignoreBaseUrl() {
        rxHttp.get("https://api.test.ignore.cn/getInfo")
                .ignoreBaseUrl()
                .addUrlParam("name", "tom")
                .toResultObservable(StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
                .bindToActivity()
    }

    /**
     * POST表单请求
     */
    private fun postNormal() {
        rxHttp.post("/updateInfo")
                .addUrlParam("name", "tom")
                .addHeader("header", "header_value")
                .addRequestParam("key", "value")
                .toResultObservable(StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
                .bindToActivity()
    }

    /**
     * POST Json请求
     */
    private fun postJson() {
        rxHttp.post("/updateInfo")
                .addUrlParam("name", "tom")
                .addHeader("header", "header_value")
                .requestBody4JSon("{\"age\":0,\"msg\":\"ok\"}")
                .toResultObservable(StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
                .bindToActivity()
    }

    /**
     * POST Text请求
     */
    private fun postText() {
        rxHttp.post("/updateInfo")
                .addUrlParam("name", "tom")
                .addHeader("header", "header_value")
                .requestBody4Text("This is a text.")
                .toResultObservable(StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
                .bindToActivity()
    }

    /**
     * 上传文件
     */
    private fun upload() {
        rxHttp.post("/updateInfo")
                .addUrlParam("name", "tom")
                .addHeader("header", "header_value")
                .requestBody4Text("This is a text.")
                .toUploadObservable(StringConverter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(object : ProgressProcessor<String>() {
                    override fun onProgress(totalSize: Long, currentSize: Long, percent: Int) {
                        Log.e(DEBUG_TAG, "${Thread.currentThread().name}: $totalSize $currentSize $percent")
                    }
                })
                .subscribe({
                    Log.e(DEBUG_TAG, "${Thread.currentThread().name}: path: $it")
                }, {
                    Log.e(DEBUG_TAG, it.message)
                })
                .bindToActivity()
    }

    /**
     * 下载文件
     */
    private fun download() {
        rxHttp.get("https://d1.music.126.net/dmusic/cloudmusicsetup_2.5.2.197409.exe")
                .ignoreBaseUrl()
                .toDownloadObservable(FileConverter(Environment.getExternalStorageDirectory(), "cloud_music_setup.exe"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(object : ProgressProcessor<File>() {
                    override fun onProgress(totalSize: Long, currentSize: Long, percent: Int) {
                        Log.e(DEBUG_TAG, "${Thread.currentThread().name}: $totalSize $currentSize $percent")
                    }
                })
                .subscribe({
                    Log.e(DEBUG_TAG, "${Thread.currentThread().name}: path: ${it.path}")
                }, {
                    Log.e(DEBUG_TAG, it.message)
                })
                .bindToActivity()
    }
}
