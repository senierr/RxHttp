package com.senierr.simple.domain

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import android.widget.TextView
import com.senierr.adapter.internal.MultiTypeAdapter
import com.senierr.adapter.internal.RVHolder
import com.senierr.adapter.internal.ViewHolderWrapper
import com.senierr.simple.R
import com.senierr.simple.remote.BmobError
import com.senierr.simple.remote.Note
import com.senierr.simple.remote.NoteService
import com.senierr.simple.util.ToastUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

/**
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class MainActivity : BaseActivity() {

    private val noteService = NoteService()
    private val multiTypeAdapter = MultiTypeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        loadData()
    }

    private fun initView() {
        btn_folder.setOnClickListener {
            startActivity(Intent(this, CloudFileActivity::class.java))
        }
        btn_add.setOnClickListener {
            insertOrReplace(null)
        }

        val noteWrapper = object : ViewHolderWrapper<Note>() {
            override fun onCreateViewHolder(p0: ViewGroup): RVHolder {
                return RVHolder.create(p0, R.layout.item_note)
            }

            override fun onBindViewHolder(p0: RVHolder, p1: Note) {
                val tvContent = p0.getView<TextView>(R.id.tv_content)
                val tvDate = p0.getView<TextView>(R.id.tv_date)

                tvContent.text = p1.content
                tvDate.text = p1.updatedAt
            }
        }

        noteWrapper.onItemClickListener = object : ViewHolderWrapper.OnItemClickListener() {
            override fun onClick(viewHolder: RVHolder?, position: Int) {
                val note = multiTypeAdapter.dataList[position] as Note
                insertOrReplace(note)
            }

            override fun onLongClick(viewHolder: RVHolder?, position: Int): Boolean {
                val note = multiTypeAdapter.dataList[position] as Note
                note.objectId?.let {
                    delete(it)
                }
                return true
            }
        }
        multiTypeAdapter.bind(Note::class.java, noteWrapper)

        rv_notes.layoutManager = LinearLayoutManager(this)
        rv_notes.adapter = multiTypeAdapter
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        noteService.getAll()
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
     * 新增或更新
     */
    private fun insertOrReplace(note: Note?) {
        val editDialog = EditDialog(this, note?: Note(), object : EditDialog.OnEditListener {
            override fun onConfirm(note: Note) {
                Observable.just(note)
                        .flatMap {
                            if (it.objectId == null && it.content != null) {
                                return@flatMap noteService.insert(it.content!!)
                            } else {
                                return@flatMap noteService.update(it)
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            loadData()
                        }, {
                            if (it is BmobError) {
                                ToastUtil.showShort(this@MainActivity, it.error)
                            } else {
                                ToastUtil.showShort(this@MainActivity, R.string.network_error)
                            }
                        })
                        .bindToLifecycle()
            }
        })
        editDialog.show()
    }

    /**
     * 删除
     */
    private fun delete(objectId: String) {
        noteService.delete(objectId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    loadData()
                }, {
                    if (it is BmobError) {
                        ToastUtil.showShort(this@MainActivity, it.error)
                    } else {
                        ToastUtil.showShort(this@MainActivity, R.string.network_error)
                    }
                })
                .bindToLifecycle()
    }
}