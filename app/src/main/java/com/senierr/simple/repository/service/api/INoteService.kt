package com.senierr.simple.repository.service.api

import com.senierr.simple.repository.bean.BmobDelete
import com.senierr.simple.repository.bean.BmobInsert
import com.senierr.simple.repository.bean.BmobUpdate
import com.senierr.simple.repository.bean.Note
import io.reactivex.Observable

/**
 * Note网络接口
 *
 * @author zhouchunjie
 * @date 2018/9/22
 */
interface INoteService {

    /**
     * 获取数据
     */
    fun get(objectId: String): Observable<Note>

    /**
     * 获取分页数据
     */
    fun getAll(pageIndex: Int): Observable<MutableList<Note>>

    /**
     * 上传
     */
    fun insert(title: String, content: String): Observable<BmobInsert>

    /**
     * 更新
     */
    fun update(note: Note): Observable<BmobUpdate>

    /**
     * 删除
     */
    fun delete(objectId: String): Observable<BmobDelete>
}