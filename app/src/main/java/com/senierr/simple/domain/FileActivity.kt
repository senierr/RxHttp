package com.senierr.simple.domain

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.senierr.adapter.internal.MultiTypeAdapter
import com.senierr.simple.R
import com.senierr.simple.remote.BmobError
import com.senierr.simple.remote.CloudFile
import com.senierr.simple.remote.CloudFileService
import com.senierr.simple.util.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_file.*

/**
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class FileActivity : BaseActivity() {

    private val cloudFileService = CloudFileService()
    private val multiTypeAdapter = MultiTypeAdapter()
    private val cloudFileWrapper = CloudFileWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        initView()
        loadData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun initView() {
        btn_left.setOnClickListener {
            finish()
        }
        btn_upload.setOnClickListener {

        }

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

    private fun upload() {

    }
}