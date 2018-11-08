package com.senierr.simple.domain

import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.senierr.adapter.internal.ViewHolder
import com.senierr.adapter.internal.ViewHolderWrapper
import com.senierr.simple.R
import com.senierr.simple.remote.CloudFile
import com.senierr.simple.remote.DownloadProgress

/**
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class CloudFileWrapper : ViewHolderWrapper<CloudFile>() {

    val statusMap : MutableMap<Int, DownloadProgress> = mutableMapOf()

    override fun onCreateViewHolder(p0: ViewGroup): ViewHolder {
        return ViewHolder.create(p0, R.layout.item_file)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: CloudFile) {
        val tvName = p0.findView<TextView>(R.id.tv_name)
        val pbBar = p0.findView<ProgressBar>(R.id.pb_bar)
        val btnOperate = p0.findView<Button>(R.id.btn_operate)

        var downloadProgress = statusMap[p0.layoutPosition]
        if (downloadProgress == null) {
            downloadProgress = DownloadProgress(p1.url, 0, 0, 0, DownloadProgress.STATUS_UN_DOWNLOAD)
            statusMap[p0.layoutPosition] = downloadProgress
        }

        tvName.text = p1.filename
        when (downloadProgress.status) {
            DownloadProgress.STATUS_UN_DOWNLOAD -> {
                pbBar.progress = 0
                btnOperate.setText(R.string.start)
            }
            DownloadProgress.STATUS_DOWNLOADING -> {
                pbBar.progress = downloadProgress.percent
                btnOperate.setText(R.string.cancel)
            }
            DownloadProgress.STATUS_PAUSE ->
                btnOperate.setText(R.string.start)
            DownloadProgress.STATUS_COMPLETED -> {
                pbBar.progress = 100
                btnOperate.setText(R.string.start)
            }
        }
    }
}