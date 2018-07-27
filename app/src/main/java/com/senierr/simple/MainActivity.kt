package com.senierr.simple

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.senierr.permission.CheckCallback
import com.senierr.permission.PermissionManager
import com.senierr.sehttp.callback.FileCallback
import com.senierr.sehttp.callback.StringCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.Exception

/**
 * @author zhouchunjie
 * @date 2018/5/17
 */
class MainActivity : AppCompatActivity() {

    private val logTag = MainActivity::class.java.name
    private val seHttp = SeApplication.instance.getHttp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 检查权限
        PermissionManager.with(this)
                .permissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .request(object : CheckCallback() {
                    override fun onAllGranted() {
                        // 初始化界面
                        initView()
                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        seHttp.cancelTag(this)
    }

    /**
     * 初始化界面
     */
    private fun initView() {
        btn_get.setOnClickListener { get() }
        btn_post_form.setOnClickListener { postForm() }
        btn_post_json.setOnClickListener { postJSon() }
        btn_upload.setOnClickListener { upload() }
        btn_download.setOnClickListener { download() }
    }

    /**
     * GET请求
     */
    private fun get() {
        seHttp.get(URL_GET)
                .tag(this)
                .addUrlParam("ip", "112.64.217.29")
                .addHeader("language", "Chinese")
                .execute(object : StringCallback() {
                    override fun onSuccess(t: String?) {
                        Log.e(logTag, "--success: $t")
                    }

                    override fun onFailure(e: Exception?) {
                        Log.e(logTag, "--onError: ${Log.getStackTraceString(e)}")
                    }
                })
    }

    /**
     * POST表单请求
     */
    private fun postForm() {
        val urlParams = LinkedHashMap<String, String>()
        urlParams["custom_url_param_1"] = "custom_url_param_value_1"
        urlParams["custom_url_param_2"] = "custom_url_param_value_2"

        val headers = LinkedHashMap<String, String>()
        headers["custom_header_1"] = "custom_header_value_1"
        headers["custom_header_2"] = "custom_header_value_2"

        val params = LinkedHashMap<String, String>()
        params["custom_params_1"] = "custom_params_value_1"
        params["custom_params_2"] = "custom_params_value_2"

        seHttp.post(URL_POST_FORM)
                .tag(this)
                .addHeader("header", "header_value")
                .addHeaders(headers)
                .addUrlParam("url_param", "url_param_value")
                .addUrlParams(urlParams)
                .addRequestParam("param", "value")
                .addRequestStringParams(params)
                .execute(object : StringCallback() {
                    override fun onSuccess(t: String?) {
                        Log.e(logTag, "--success: $t")
                    }

                    override fun onFailure(e: Exception?) {
                        Log.e(logTag, "--onError: ${Log.getStackTraceString(e)}")
                    }
                })
    }

    /**
     * POST JSON请求
     */
    private fun postJSon() {
        seHttp.post(URL_POST_FORM)
                .tag(this)
                .addHeader("header", "header_value")
                .addUrlParam("url_param", "url_param_value")
                .setRequestBody4JSon("{'name':'test'}")
                .execute(object : StringCallback() {
                    override fun onSuccess(t: String?) {
                        Log.e(logTag, "--success: $t")
                    }

                    override fun onFailure(e: Exception?) {
                        Log.e(logTag, "--onError: ${Log.getStackTraceString(e)}")
                    }
                })
    }

    /**
     * 上传请求
     */
    private fun upload() {
        val destFile = File(Environment.getExternalStorageDirectory(), "Desert.jpg")
        seHttp.post(URL_UPLOAD)
                .tag(this)
                .addRequestParam("file", destFile)
                .execute(object : StringCallback() {
                    override fun onUpload(progress: Int, currentSize: Long, totalSize: Long) {
                        Log.e(logTag, "--onUpload: $progress")
                    }

                    override fun onSuccess(t: String?) {
                        Log.e(logTag, "--success: $t")
                    }

                    override fun onFailure(e: Exception?) {
                        Log.e(logTag, "--onError: ${Log.getStackTraceString(e)}")
                    }
                })
    }

    /**
     * 下载请求
     */
    private fun download() {
        val destFile = File(Environment.getExternalStorageDirectory(), "WeChat.exe")
        seHttp.post(URL_DOWNLOAD)
                .tag(this)
                .execute(object : FileCallback(destFile) {
                    override fun onDownload(progress: Int, currentSize: Long, totalSize: Long) {
                        Log.e(logTag, "--onDownload: $progress")
                    }

                    override fun onSuccess(t: File?) {
                        Log.e(logTag, "--onSuccess: ${t?.path}")
                    }

                    override fun onFailure(e: Exception?) {
                        Log.e(logTag, "--onError: ${Log.getStackTraceString(e)}")
                    }
                })
    }
}