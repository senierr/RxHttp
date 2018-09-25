package com.senierr.simple.domain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.senierr.simple.R
import com.senierr.simple.domain.base.BaseActivity
import kotlinx.android.synthetic.main.layout_top_bar_normal.*

/**
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class NoteActivity : BaseActivity() {

    private var objectId: String? = null

    companion object {
        fun start(context: Context, objectId: String) {
            val intent = Intent(context, NoteActivity::class.java)
            intent.putExtra("objectId", objectId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        objectId = intent.getStringExtra("objectId")

        initView()
    }

    private fun initView() {
        btn_left.visibility = View.GONE
        tv_title.setText(R.string.app_name)
        btn_right.setImageResource(R.drawable.ic_done_black_24dp)
        btn_right.visibility = View.VISIBLE
        btn_right.setOnClickListener {

        }
    }
}