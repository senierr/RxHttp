package com.senierr.simple.domain

import android.support.v7.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Activity基类
 *
 * @author zhouchunjie
 * @date 2018/5/28
 */
open class BaseActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val disposableMap = mutableMapOf<Any, Disposable>()

    override fun onDestroy() {
        unsubscribeAll()
        super.onDestroy()
    }

    fun Disposable.bindToLifecycle(tag: Any? = null): Disposable {
        if (tag == null) {
            compositeDisposable.add(this)
        } else {
            disposableMap[tag] = this
        }
        return this
    }

    fun unsubscribe(tag: Any) {
        disposableMap[tag]?.dispose()
        disposableMap.remove(tag)
    }

    fun unsubscribeAll() {
        disposableMap.forEach { _, disposable ->
            disposable.dispose()
        }
        compositeDisposable.clear()
    }
}