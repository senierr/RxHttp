package com.senierr.simple

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 *
 * @author zhouchunjie
 * @date 2018/5/17
 */
class MainActivity : AppCompatActivity() {

    val URL_DOWNLOAD = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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