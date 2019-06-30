package com.senierr.simple

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

/**
 *
 * @author zhouchunjie
 * @date 2018/9/23
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        btn_go_home.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}