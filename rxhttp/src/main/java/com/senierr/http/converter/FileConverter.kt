package com.senierr.http.converter

import java.io.File
import java.io.IOException

import io.reactivex.annotations.NonNull
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio

/**
 * 文件下载转换器
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
class FileConverter : Converter<File> {

    private var destDir: File
    private var destName: String

    constructor(destFile: File) {
        this.destDir = destFile.parentFile
        this.destName = destFile.name
    }

    constructor(destDir: File, destName: String) {
        this.destDir = destDir
        this.destName = destName
    }

    override fun convertResponse(response: Response): File {
        // 判断路径是否存在
        if (!destDir.exists()) {
            val result = destDir.mkdirs()
            if (!result) {
                throw Exception(destDir.path + " create failed!")
            }
        }

        val destFile = File(destDir, destName)
        // 判断文件是否存在
        if (destFile.exists()) {
            val result = destFile.delete()
            if (!result) {
                throw Exception(destFile.path + " delete failed!")
            }
        }

        var bufferedSource: BufferedSource? = null
        var bufferedSink: BufferedSink? = null
        try {
            val responseBody = response.body() ?: throw IOException("ResponseBody is null!")

            bufferedSource = Okio.buffer(Okio.source(responseBody.byteStream()))
            bufferedSink = Okio.buffer(Okio.sink(destFile))

            val bytes = ByteArray(1024)
            var len: Int
            while (bufferedSource.read(bytes).also { len = it } != -1) {
                bufferedSink.write(bytes, 0, len)
            }
            bufferedSink.flush()
            return destFile
        } finally {
            Util.closeQuietly(bufferedSource)
            Util.closeQuietly(bufferedSink)
        }
    }
}
