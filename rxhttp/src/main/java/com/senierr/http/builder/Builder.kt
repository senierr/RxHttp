package com.senierr.http.builder

/**
 * 基础构建器
 *
 * @author zhouchunjie
 * @date 2019/6/28
 */
interface Builder<T> {

    fun build(): T
}