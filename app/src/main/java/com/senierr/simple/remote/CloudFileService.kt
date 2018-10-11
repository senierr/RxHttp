package com.senierr.simple.remote

import com.google.gson.Gson
import com.senierr.http.converter.FileConverter
import com.senierr.http.internal.Result
import com.senierr.simple.app.SessionApplication
import io.reactivex.Observable
import java.io.File

/**
 * File模块
 *
 * @author zhouchunjie
 * @date 2018/9/22
 */
class CloudFileService {

    companion object {
        private const val API_FILE_SERVICE = "https://api.bmob.cn/2/files"
        private const val API_FILE = "https://api.bmob.cn/1/classes/file"
    }

    fun getAll(): Observable<MutableList<CloudFile>> {
        return SessionApplication.application.dataHttp
                .get(API_FILE)
                .addUrlParam("order", "updatedAt")
                .execute(BmobArrayConverter(CloudFile::class.java))
                .map {
                    it.body()?.results
                }
    }

    fun insert(bmobFile: BmobFile): Observable<BmobInsert> {
        val param = mapOf(
                Pair("filename", bmobFile.filename),
                Pair("url", bmobFile.url),
                Pair("cdn", bmobFile.cdn)
        )
        return SessionApplication.application.dataHttp
                .post(API_FILE)
                .setRequestBody4JSon(Gson().toJson(param))
                .execute(BmobObjectConverter(BmobInsert::class.java))
                .map {
                    it.body()
                }
    }

    fun delete(objectId: String): Observable<BmobDelete> {
        return SessionApplication.application.dataHttp
                .delete("$API_FILE/$objectId")
                .execute(BmobObjectConverter(BmobDelete::class.java))
                .map {
                    it.body()
                }
    }

    fun upload(file: File): Observable<Result<BmobInsert>> {
        return SessionApplication.application.dataHttp
                .post("$API_FILE_SERVICE/${file.name}")
                .addRequestParam(file.name, file)
                .openUploadListener(true)
                .execute(BmobObjectConverter(BmobFile::class.java))
                .flatMap {
                    if (it.uploadProgress() != null) {
                        return@flatMap Observable.just(Result.upload<BmobInsert>(it.uploadProgress()!!))
                    } else if (it.response() != null) {
                        val param = mapOf(
                                Pair("filename", it.body()?.filename),
                                Pair("url", it.body()?.url),
                                Pair("cdn", it.body()?.cdn)
                        )
                        return@flatMap SessionApplication.application.dataHttp
                                .post(API_FILE)
                                .setRequestBody4JSon(Gson().toJson(param))
                                .execute(BmobObjectConverter(BmobInsert::class.java))
                    } else {
                        return@flatMap Observable.just(Result.download<BmobInsert>(it.downloadProgress()!!))
                    }
                }
    }

    fun download(url: String, destFile: File): Observable<Result<File>> {
        return SessionApplication.application.dataHttp
                .get(url)
                .openDownloadListener(true)
                .execute(FileConverter(destFile))
    }
}