package com.senierr.simple.domain

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.ProgressBar
import android.widget.TextView
import com.senierr.simple.R

/**
 * 进度Dialog
 *
 * @author zhouchunjie
 * @date 2018/2/27
 */
class UploadDialog(context: Context, val content: String) : AlertDialog(context, R.style.BaseDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_upload)
        setCancelable(true)
        setCanceledOnTouchOutside(false)

        val tvContent = findViewById<TextView>(R.id.tv_content)
        tvContent?.text = content
    }

    fun updateProgress(progress: Int) {
        val pbBar = findViewById<ProgressBar>(R.id.pb_bar)
        pbBar?.progress = progress
    }
}