package com.senierr.http.builder

/**
 *
 * @author zhouchunjie
 * @date 2019/6/28
 */
interface Builder<T> {

    fun build(): T
}