package com.senierr.simple.domain

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.WindowManager
import com.senierr.simple.R
import com.senierr.simple.remote.Note
import kotlinx.android.synthetic.main.dialog_edit.*

/**
 * 编辑Dialog
 *
 * @author zhouchunjie
 * @date 2018/2/27
 */
class EditDialog(
        context: Context,
        private var note: Note = Note(),
        private var onEditListener: OnEditListener? = null
) : AlertDialog(context, R.style.BaseDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        btn_cancel.setOnClickListener {
            cancel()
        }
        btn_confirm.setOnClickListener {
            val text = et_edit.text.toString()
            if (!TextUtils.isEmpty(text)) {
                note.content = text
                onEditListener?.onConfirm(note)
                cancel()
            }
        }

        note.content?.let {
            et_edit.text.append(it)
        }
    }

    interface OnEditListener {
        fun onConfirm(note: Note)
    }
}