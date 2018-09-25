package com.senierr.simple.domain

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.senierr.adapter.internal.MultiTypeAdapter
import com.senierr.adapter.internal.RVHolder
import com.senierr.adapter.internal.ViewHolderWrapper
import com.senierr.adapter.support.bean.LoadMoreBean
import com.senierr.simple.R
import com.senierr.simple.domain.base.BaseActivity
import com.senierr.simple.domain.wrapper.LoadMoreWrapper
import com.senierr.simple.domain.wrapper.NoteWrapper
import com.senierr.simple.repository.Repository
import com.senierr.simple.repository.bean.BmobError
import com.senierr.simple.repository.bean.Note
import com.senierr.simple.repository.service.api.INoteService
import com.senierr.simple.util.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_top_bar_normal.*

/**
 * 首页
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class MainActivity : BaseActivity() {

    private val noteService = Repository.getService<INoteService>()

    private val multiTypeAdapter = MultiTypeAdapter()
    private val noteWrapper = NoteWrapper()
    private val loadMoreWrapper = LoadMoreWrapper()
    private var currentPageIndex = 0
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        srl_refresh.postDelayed({
            srl_refresh.isRefreshing = true
            currentPageIndex = 0
            loadData()
        }, 200)
    }

    private fun initView() {
        btn_left.visibility = View.GONE
        tv_title.setText(R.string.app_name)
        btn_right.setImageResource(R.drawable.ic_add_black_24dp)
        btn_right.visibility = View.VISIBLE
        btn_right.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        noteWrapper.onItemClickListener = object : ViewHolderWrapper.OnItemClickListener() {
            override fun onClick(viewHolder: RVHolder?, position: Int) {
//                val user = multiTypeAdapter.dataList[position] as User
//                UserInfoActivity.openUserInfo(this@MainActivity, user.objectId, false)
            }
        }
        multiTypeAdapter.bind(Note::class.java, noteWrapper)
        multiTypeAdapter.bind(LoadMoreBean::class.java, loadMoreWrapper)

        rv_notes.layoutManager = LinearLayoutManager(this)
        rv_notes.adapter = multiTypeAdapter

        srl_refresh.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary)
        srl_refresh.setOnRefreshListener {
            currentPageIndex = 0
            loadData()
        }
    }

    private fun loadData() {
        disposable?.dispose()
        disposable = noteService.getAll(currentPageIndex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (currentPageIndex == 0) {
                        multiTypeAdapter.dataList.clear()
                        multiTypeAdapter.dataList.addAll(it)
                        multiTypeAdapter.dataList.add(loadMoreWrapper.loadMoreBean)
                        multiTypeAdapter.notifyDataSetChanged()
                        srl_refresh.isRefreshing = false
                        currentPageIndex++
                    } else {
                        if (it.isEmpty()) {
                            loadMoreWrapper.loadMoreNoMore()
                        } else {
                            val startPosition = multiTypeAdapter.dataList.size - 1
                            multiTypeAdapter.dataList.addAll(startPosition, it)
                            multiTypeAdapter.notifyItemRangeInserted(startPosition, multiTypeAdapter.dataList.size)
                            currentPageIndex++
                        }
                    }
                }, {
                    if (it is BmobError) {
                        ToastUtil.showShort(this, it.error)
                    } else {
                        ToastUtil.showShort(this, R.string.network_error)
                    }
                })
                .bindToLifecycle()
    }
}