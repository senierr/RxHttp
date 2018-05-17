package com.senierr.simple

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.senierr.sehttp.SeHttp
import com.senierr.sehttp.converter.StringConverter
import com.senierr.sehttp.interceptor.LogLevel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

/**
 *
 * @author zhouchunjie
 * @date 2018/5/17
 */
class MainActivity : AppCompatActivity() {

    val TAG_LOG = MainActivity::class.java.name

    val URL_DOWNLOAD = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe"
    lateinit var seHttp: SeHttp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 初始化界面
        initView()
        // 初始化SeHttp
        seHttp = SeHttp.Builder()
                .setDebug(TAG_LOG, LogLevel.BODY)
                .setConnectTimeout(10 * 1000)
                .setReadTimeout(10 * 1000)
                .setWriteTimeout(10 * 1000)
                .addCommonHeader("com_header", "com_header_value")
                .addCommonUrlParam("com_url_param", "com_url_param_value")
                .build()
    }

    /**
     * 初始化界面
     */
    private fun initView() {
        btn_get.setOnClickListener { get() }
        btn_post.setOnClickListener { post() }
        btn_https.setOnClickListener { https() }
        btn_upload.setOnClickListener { upload() }
        btn_download.setOnClickListener { download() }
    }

    private fun get() {
        Single.fromCallable {
            return@fromCallable seHttp.get("http://www.baidu.com")
                    .addHeader("header", "header_value")
                    .addUrlParam("url_param", "url_param_value")
                    .execute(StringConverter())
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.e(TAG_LOG, "success: $it")
                }, {
                    Log.e(TAG_LOG, "success: ${Log.getStackTraceString(it)}")
                })
    }

    private fun post() {

    }

    private fun https() {

    }

    private fun upload() {

    }

    private fun download() {

    }
}