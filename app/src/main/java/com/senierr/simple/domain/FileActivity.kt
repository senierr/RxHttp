package com.senierr.simple.domain

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.senierr.adapter.internal.MultiTypeAdapter
import com.senierr.adapter.internal.RVHolder
import com.senierr.adapter.internal.ViewHolderWrapper
import com.senierr.permission.CheckCallback
import com.senierr.permission.PermissionManager
import com.senierr.simple.R
import com.senierr.simple.remote.BmobError
import com.senierr.simple.remote.CloudFile
import com.senierr.simple.remote.CloudFileService
import com.senierr.simple.remote.DownloadProgress
import com.senierr.simple.util.ToastUtil
import com.senierr.simple.util.UriUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_file.*
import java.io.File


/**
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class FileActivity : BaseActivity() {

    companion object {
        const val REQUEST_CODE_PICK_FILE = 1001
    }

    private val cloudFileService = CloudFileService()
    private val multiTypeAdapter = MultiTypeAdapter()
    private val cloudFileWrapper = CloudFileWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        PermissionManager.with(this)
                .permissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .request(object : CheckCallback() {
                    override fun onAllGranted() {
                        initView()
                        loadData()
                    }

                    override fun onDenied(deniedWithNextAskList: MutableList<String>?,
                                          deniedWithNoAskList: MutableList<String>?) {
                        finish()
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            data?.data?.let {
                upload(UriUtil.getPath(this, it))
            }
        }
    }

    private fun initView() {
        btn_left.setOnClickListener {
            finish()
        }
        btn_upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
        }

        cloudFileWrapper.setOnItemChildClickListener(R.id.btn_operate,
                object : ViewHolderWrapper.OnItemChildClickListener() {
            override fun onClick(viewHolder: RVHolder?, view: View?, position: Int) {
                val downloadProgress = cloudFileWrapper.statusMap[position]
                when (downloadProgress!!.status) {
                    DownloadProgress.STATUS_UN_DOWNLOAD -> download(position)
                    DownloadProgress.STATUS_DOWNLOADING -> cancelDownload(position)
                    DownloadProgress.STATUS_PAUSE -> download(position)
                    DownloadProgress.STATUS_COMPLETED -> download(position)
                }
            }
        })
        multiTypeAdapter.bind(CloudFile::class.java, cloudFileWrapper)

        rv_files.layoutManager = LinearLayoutManager(this)
        rv_files.adapter = multiTypeAdapter
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        cloudFileService.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    multiTypeAdapter.dataList.clear()
                    multiTypeAdapter.dataList.addAll(it)
                    multiTypeAdapter.notifyDataSetChanged()
                }, {
                    if (it is BmobError) {
                        ToastUtil.showShort(this, it.error)
                    } else {
                        ToastUtil.showShort(this, R.string.network_error)
                    }
                })
                .bindToLifecycle()
    }

    /**
     * 上传文件
     */
    private fun upload(path: String) {
        val uploadDialog = UploadDialog(this, path)
        uploadDialog.setOnCancelListener {
            unsubscribe("upload")
        }
        uploadDialog.show()
        cloudFileService.upload(File(path))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    uploadDialog.cancel()
                    loadData()
                }
                .subscribe({
                    it.uploadProgress()?.let { progress ->
                        uploadDialog.updateProgress(progress.percent())
                    }
                    it.body()?.let { _ ->
                        ToastUtil.showShort(this, R.string.upload_success)
                    }
                }, {
                    if (it is BmobError) {
                        ToastUtil.showShort(this, it.error)
                    } else {
                        ToastUtil.showShort(this, R.string.network_error)
                    }
                })
                .bindToLifecycle("upload")
    }

    /**
     * 下载
     */
    private fun download(position: Int) {
        val cloudFile = multiTypeAdapter.dataList[position] as CloudFile
        val destFile = File(externalCacheDir, cloudFile.filename)

        cloudFileService.download(cloudFile.url, destFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    val downloadProgress = cloudFileWrapper.statusMap[position]
                    downloadProgress?.currentSize = 0
                    downloadProgress?.totalSize = 0
                    downloadProgress?.percent = 0
                    downloadProgress?.status = DownloadProgress.STATUS_DOWNLOADING
                    multiTypeAdapter.notifyItemChanged(position)
                }
                .subscribe({
                    it.downloadProgress()?.let { progress ->
                        val downloadProgress = cloudFileWrapper.statusMap[position]
                        downloadProgress?.currentSize = progress.currentSize()
                        downloadProgress?.totalSize = progress.totalSize()
                        downloadProgress?.percent = progress.percent()
                        downloadProgress?.status = DownloadProgress.STATUS_DOWNLOADING
                        multiTypeAdapter.notifyItemChanged(position)
                    }
                    it.body()?.let { _ ->
                        val downloadProgress = cloudFileWrapper.statusMap[position]
                        downloadProgress?.status = DownloadProgress.STATUS_COMPLETED
                        multiTypeAdapter.notifyItemChanged(position)
                        ToastUtil.showShort(this, R.string.download_success)
                    }
                }, {
                    if (it is BmobError) {
                        ToastUtil.showShort(this, it.error)
                    } else {
                        ToastUtil.showShort(this, R.string.network_error)
                    }
                })
                .bindToLifecycle("download_$position")
    }

    /**
     * 取消下载
     */
    private fun cancelDownload(position: Int) {
        unsubscribe("download_$position")
        val downloadProgress = cloudFileWrapper.statusMap[position]
        downloadProgress?.percent = 0
        downloadProgress?.status = DownloadProgress.STATUS_UN_DOWNLOAD
        multiTypeAdapter.notifyItemChanged(position)
    }
}