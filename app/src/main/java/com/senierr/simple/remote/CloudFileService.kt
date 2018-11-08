package com.senierr.simple.remote

import com.google.gson.Gson
import com.senierr.http.converter.FileConverter
import com.senierr.http.internal.OnProgressListener
import com.senierr.http.internal.Response
import com.senierr.simple.app.SessionApplication
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
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
                    it.body().results
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

    fun upload(file: File, onUploadListener: OnProgressListener): Observable<BmobInsert> {
        return SessionApplication.application.dataHttp
                .post("$API_FILE_SERVICE/${file.name}")
                .setRequestBody4File(file)
                .setOnUploadListener(onUploadListener)
                .setUploadListenerOn(AndroidSchedulers.mainThread())
                .execute(BmobObjectConverter(BmobFile::class.java))
                .flatMap {
                    return@flatMap insert(it.body())
                }
    }

    fun download(url: String, destFile: File, onDownloadListener: OnProgressListener): Observable<Response<File>> {
        return SessionApplication.application.dataHttp
                .get(url)
                .setOnDownloadListener(onDownloadListener)
                .setDownloadListenerOn(AndroidSchedulers.mainThread())
                .execute(FileConverter(destFile))
    }
}