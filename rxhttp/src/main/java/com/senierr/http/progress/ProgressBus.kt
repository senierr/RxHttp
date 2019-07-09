package com.senierr.http.progress

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * 进度总栈
 *
 * @author zhouchunjie
 * @date 2019/7/9
 */
object ProgressBus {

    private val bus = PublishSubject.create<Progress>().toSerialized()

    /**
     * 发送进度
     */
    fun post(progress: Progress) {
        bus.onNext(progress)
    }

    /**
     * 订阅进度
     */
    fun toObservable(tag: String): Observable<Progress> {
        return bus.filter { it.tag == tag }
    }
}