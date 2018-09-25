package com.senierr.simple.util

import android.content.Context
import android.widget.Toast

/**
 * 吐司工具类
 *
 * @author zhouchunjie
 * @date 2018/4/8
 */
object ToastUtil {

    private var toast: Toast? = null

    /**
     * 显示短时吐司
     *
     * @param context 上下文
     * @param resId 字符串ID
     */
    fun showShort(context: Context, resId: Int) {
        if (toast == null) {
            toast = Toast.makeText(context.applicationContext, resId, Toast.LENGTH_SHORT)
        }
        toast?.setText(resId)
        toast?.duration = Toast.LENGTH_SHORT
        toast?.show()
    }

    /**
     * 显示短时吐司
     *
     * @param context 上下文
     * @param message 文本
     */
    fun showShort(context: Context, message: String?) {
        if (message == null) return
        if (toast == null) {
            toast = Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT)
        }
        toast?.setText(message)
        toast?.duration = Toast.LENGTH_SHORT
        toast?.show()
    }

    /**
     * 显示长时吐司
     *
     * @param context 上下文
     * @param resId 字符串ID
     */
    fun showLong(context: Context, resId: Int) {
        if (toast == null) {
            toast = Toast.makeText(context.applicationContext, resId, Toast.LENGTH_LONG)
        }
        toast?.setText(resId)
        toast?.duration = Toast.LENGTH_LONG
        toast?.show()
    }

    /**
     * 显示长时吐司
     *
     * @param context 上下文
     * @param message 文本
     */
    fun showLong(context: Context, message: String?) {
        if (message == null) return
        if (toast == null) {
            toast = Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG)
        }
        toast?.setText(message)
        toast?.duration = Toast.LENGTH_LONG
        toast?.show()
    }

    fun cancel() {
        if (toast != null) {
            toast?.cancel()
        }
    }
}