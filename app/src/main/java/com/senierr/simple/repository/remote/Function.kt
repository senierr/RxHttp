package com.senierr.simple.repository.remote

import com.senierr.http.internal.Result
import com.senierr.simple.repository.bean.BmobArray
import io.reactivex.functions.Function
import java.io.IOException

/**
 * 数据转换
 *
 * @author zhouchunjie
 * @date 2018/9/22
 */

/** 解析对象 */
class ObjectFunction<T> : Function<Result<T>, T> {
    override fun apply(t: Result<T>): T {
        val body = t.body()
        if (body == null) {
            throw IOException("Response body is null!")
        } else {
            return body
        }
    }
}

/** 解析列表 */
class BmobArrayFunction<T> : Function<Result<BmobArray<T>>, MutableList<T>> {
    override fun apply(t: Result<BmobArray<T>>): MutableList<T> {
        val result = t.body()?.results
        if (result == null) {
            throw IOException("Response body is null!")
        } else {
            return result
        }
    }
}