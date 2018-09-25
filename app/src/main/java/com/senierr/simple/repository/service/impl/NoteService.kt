package com.senierr.simple.repository.service.impl

import com.google.gson.Gson
import com.senierr.simple.repository.Repository
import com.senierr.simple.repository.bean.BmobDelete
import com.senierr.simple.repository.bean.BmobInsert
import com.senierr.simple.repository.bean.BmobUpdate
import com.senierr.simple.repository.bean.Note
import com.senierr.simple.repository.remote.*
import com.senierr.simple.repository.service.api.INoteService
import io.reactivex.Observable

/**
 * Note模块
 *
 * @author zhouchunjie
 * @date 2018/9/22
 */
class NoteService : INoteService {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun get(objectId: String): Observable<Note> {
        return Repository.dataHttp.get("$API_NOTE/$objectId")
                .execute(BmobObjectConverter(Note::class.java))
                .map(ObjectFunction())
    }

    override fun getAll(pageIndex: Int): Observable<MutableList<Note>> {
        val skips = pageIndex * PAGE_SIZE
        return Repository.dataHttp.get(API_NOTE)
                .addUrlParam("limit", "$PAGE_SIZE")
                .addUrlParam("skip", "$skips")
                .addUrlParam("order", "updatedAt")
                .execute(BmobArrayConverter(Note::class.java))
                .map(BmobArrayFunction())
    }

    override fun insert(title: String, content: String): Observable<BmobInsert> {
        val param = mapOf(
                Pair("title", title),
                Pair("content", content)
        )
        return Repository.dataHttp.post(API_NOTE)
                .setRequestBody4JSon(Gson().toJson(param))
                .execute(BmobObjectConverter(BmobInsert::class.java))
                .map(ObjectFunction())
    }

    override fun update(note: Note): Observable<BmobUpdate> {
        return Repository.dataHttp.put("$API_NOTE/${note.objectId}")
                .setRequestBody4JSon(Gson().toJson(note))
                .execute(BmobObjectConverter(BmobUpdate::class.java))
                .map(ObjectFunction())
    }

    override fun delete(objectId: String): Observable<BmobDelete> {
        return Repository.dataHttp.delete("$API_NOTE/$objectId")
                .execute(BmobObjectConverter(BmobDelete::class.java))
                .map(ObjectFunction())
    }
}