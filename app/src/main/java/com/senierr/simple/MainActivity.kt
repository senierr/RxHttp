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

    private val TAG_LOG = MainActivity::class.java.name

    private val CER_12306 = "-----BEGIN CERTIFICATE-----\n" +
            "MIICmjCCAgOgAwIBAgIIbyZr5/jKH6QwDQYJKoZIhvcNAQEFBQAwRzELMAkGA1UEBhMCQ04xKTAn\n" +
            "BgNVBAoTIFNpbm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMB4X\n" +
            "DTA5MDUyNTA2NTYwMFoXDTI5MDUyMDA2NTYwMFowRzELMAkGA1UEBhMCQ04xKTAnBgNVBAoTIFNp\n" +
            "bm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMIGfMA0GCSqGSIb3\n" +
            "DQEBAQUAA4GNADCBiQKBgQDMpbNeb34p0GvLkZ6t72/OOba4mX2K/eZRWFfnuk8e5jKDH+9BgCb2\n" +
            "9bSotqPqTbxXWPxIOz8EjyUO3bfR5pQ8ovNTOlks2rS5BdMhoi4sUjCKi5ELiqtyww/XgY5iFqv6\n" +
            "D4Pw9QvOUcdRVSbPWo1DwMmH75It6pk/rARIFHEjWwIDAQABo4GOMIGLMB8GA1UdIwQYMBaAFHle\n" +
            "tne34lKDQ+3HUYhMY4UsAENYMAwGA1UdEwQFMAMBAf8wLgYDVR0fBCcwJTAjoCGgH4YdaHR0cDov\n" +
            "LzE5Mi4xNjguOS4xNDkvY3JsMS5jcmwwCwYDVR0PBAQDAgH+MB0GA1UdDgQWBBR5XrZ3t+JSg0Pt\n" +
            "x1GITGOFLABDWDANBgkqhkiG9w0BAQUFAAOBgQDGrAm2U/of1LbOnG2bnnQtgcVaBXiVJF8LKPaV\n" +
            "23XQ96HU8xfgSZMJS6U00WHAI7zp0q208RSUft9wDq9ee///VOhzR6Tebg9QfyPSohkBrhXQenvQ\n" +
            "og555S+C3eJAAVeNCTeMS3N/M5hzBRJAoffn3qoYdAO1Q8bTguOi+2849A==\n" +
            "-----END CERTIFICATE-----"

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
                    .executeWith(StringConverter())
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