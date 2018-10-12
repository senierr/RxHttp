package com.senierr.simple.domain

import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.senierr.adapter.internal.RVHolder
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

    override fun onCreateViewHolder(p0: ViewGroup): RVHolder {
        return RVHolder.create(p0, R.layout.item_file)
    }

    override fun onBindViewHolder(p0: RVHolder, p1: CloudFile) {
        val tvName = p0.getView<TextView>(R.id.tv_name)
        val pbBar = p0.getView<ProgressBar>(R.id.pb_bar)
        val btnOperate = p0.getView<Button>(R.id.btn_operate)

        tvName.text = p1.filename
        val downloadProgress = statusMap[p0.layoutPosition]
        if (downloadProgress == null) {
            pbBar.progress = 0
            btnOperate.setText(R.string.start)
        } else {
            when (downloadProgress.status) {
                DownloadProgress.STATUS_START -> {
                    pbBar.progress = downloadProgress.percent
                    btnOperate.setText(R.string.pause)
                }
                DownloadProgress.STATUS_PAUSE ->
                    btnOperate.setText(R.string.start)
                DownloadProgress.STATUS_COMPLETED ->
                    btnOperate.setText(R.string.start)
            }
        }
    }
}