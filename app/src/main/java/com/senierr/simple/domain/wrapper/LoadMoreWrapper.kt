package com.senierr.simple.domain.wrapper

import android.view.ViewGroup
import com.senierr.adapter.internal.RVHolder
import com.senierr.adapter.support.bean.LoadMoreBean
import com.senierr.adapter.support.wrapper.BaseLoadMoreWrapper
import com.senierr.simple.R

/**
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class LoadMoreWrapper : BaseLoadMoreWrapper() {

    override fun onCreateViewHolder(p0: ViewGroup): RVHolder {
        return RVHolder.create(p0, R.layout.item_load_more)
    }

    override fun onBindViewHolder(p0: RVHolder, p1: LoadMoreBean) {
        when (p1.loadState) {
            LoadMoreBean.STATUS_LOADING -> {
                p0.setText(R.id.tv_text, R.string.loading)
                p0.setGone(R.id.pb_bar, false)
            }
            LoadMoreBean.STATUS_LOADING_COMPLETED -> {
                p0.setText(R.id.tv_text, R.string.completed)
                p0.setGone(R.id.pb_bar, true)
            }
            LoadMoreBean.STATUS_LOAD_NO_MORE -> {
                p0.setText(R.id.tv_text, R.string.no_more)
                p0.setGone(R.id.pb_bar, true)
            }
            LoadMoreBean.STATUS_LOAD_FAILURE -> {
                p0.setText(R.id.tv_text, R.string.failure)
                p0.setGone(R.id.pb_bar, true)
            }
        }
    }
}