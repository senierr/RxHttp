package com.senierr.simple.remote

import com.google.gson.Gson
import com.senierr.simple.app.SessionApplication
import io.reactivex.Observable

/**
 * Note模块
 *
 * @author zhouchunjie
 * @date 2018/9/22
 */
class NoteService {

    companion object {
        private const val API_NOTE = "https://api.bmob.cn/1/classes/note"
    }

    fun get(objectId: String): Observable<Note> {
        return SessionApplication.application.dataHttp
                .get("$API_NOTE/$objectId")
                .execute(BmobObjectConverter(Note::class.java))
                .map {
                    it.body()
                }
    }

    fun getAll(): Observable<MutableList<Note>> {
        return SessionApplication.application.dataHttp
                .get(API_NOTE)
                .addUrlParam("order", "updatedAt")
                .execute(BmobArrayConverter(Note::class.java))
                .map {
                    it.body()?.results
                }
    }

    fun insert(content: String): Observable<BmobInsert> {
        val param = mapOf(Pair("content", content))
        return SessionApplication.application.dataHttp
                .post(API_NOTE)
                .setRequestBody4JSon(Gson().toJson(param))
                .execute(BmobObjectConverter(BmobInsert::class.java))
                .map {
                    it.body()
                }
    }

    fun update(note: Note): Observable<BmobUpdate> {
        val param = mapOf(Pair("content", note.content))
        return SessionApplication.application.dataHttp
                .put("$API_NOTE/${note.objectId}")
                .setRequestBody4JSon(Gson().toJson(param))
                .execute(BmobObjectConverter(BmobUpdate::class.java))
                .map {
                    it.body()
                }
    }

    fun delete(objectId: String): Observable<BmobDelete> {
        return SessionApplication.application.dataHttp
                .delete("$API_NOTE/$objectId")
                .execute(BmobObjectConverter(BmobDelete::class.java))
                .map {
                    it.body()
                }
    }
}