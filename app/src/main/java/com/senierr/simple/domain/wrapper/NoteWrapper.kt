package com.senierr.simple.domain.wrapper

import android.view.ViewGroup
import android.widget.TextView
import com.senierr.adapter.internal.RVHolder
import com.senierr.adapter.internal.ViewHolderWrapper
import com.senierr.simple.R
import com.senierr.simple.repository.bean.Note

/**
 * 列表适配器
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class NoteWrapper : ViewHolderWrapper<Note>() {
    override fun onCreateViewHolder(p0: ViewGroup): RVHolder {
        return RVHolder.create(p0, R.layout.item_note)
    }

    override fun onBindViewHolder(p0: RVHolder, p1: Note) {
        val tvTitle = p0.getView<TextView>(R.id.tv_title)
        val tvContent = p0.getView<TextView>(R.id.tv_content)
        val tvDate = p0.getView<TextView>(R.id.tv_date)

        tvTitle.text = p1.title
        tvContent.text = p1.content
        tvDate.text = p1.updatedAt
    }
}