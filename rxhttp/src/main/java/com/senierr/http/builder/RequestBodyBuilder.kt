package com.senierr.http.builder

import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.URLConnection

/**
 * Http请求体构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
class RequestBodyBuilder : Builder<RequestBody?> {

    companion object {
        private const val MEDIA_TYPE_PLAIN = "text/plain; charset=utf-8"
        private const val MEDIA_TYPE_XML = "text/xml; charset=utf-8"
        private const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
        private const val MEDIA_TYPE_STREAM = "application/octet-stream"
    }

    private var requestBody: RequestBody? = null

    private var mediaType: MediaType? = null
    private var stringContent: String? = null
    private var bytes: ByteArray? = null
    private var file: File? = null

    private var isMultipart = false
    private val fileParams = LinkedHashMap<String, File>()
    private val stringParams = LinkedHashMap<String, String>()

    fun setRequestBody(requestBody: RequestBody) {
        this.requestBody = requestBody
    }

    fun setRequestBody4Text(textStr: String) {
        stringContent = textStr
        mediaType = MediaType.parse(MEDIA_TYPE_PLAIN)
    }

    fun setRequestBody4JSon(jsonStr: String) {
        stringContent = jsonStr
        mediaType = MediaType.parse(MEDIA_TYPE_JSON)
    }

    fun setRequestBody4Xml(xmlStr: String) {
        stringContent = xmlStr
        mediaType = MediaType.parse(MEDIA_TYPE_XML)
    }

    fun setRequestBody4Byte(bytes: ByteArray) {
        this.bytes = bytes
        mediaType = MediaType.parse(MEDIA_TYPE_STREAM)
    }

    fun setRequestBody4File(file: File) {
        this.file = file
        mediaType = guessMimeType(file.path)
    }

    fun isMultipart(isMultipart: Boolean) {
        this.isMultipart = isMultipart
    }

    fun addRequestParam(key: String, value: String) {
        stringParams[key] = value
    }

    fun addRequestStringParams(params: LinkedHashMap<String, String>) {
        stringParams.putAll(params)
    }

    fun addRequestParam(key: String, file: File) {
        fileParams[key] = file
    }

    fun addRequestFileParams(params: LinkedHashMap<String, File>) {
        fileParams.putAll(params)
    }

    override fun build(): RequestBody? {
        if (requestBody != null) {
            // 自定义
            return requestBody
        } else if (stringContent != null && mediaType != null) {
            // 字符串
            return RequestBody.create(mediaType, stringContent!!)
        } else if (bytes != null && mediaType != null) {
            // 字节数组
            return RequestBody.create(mediaType, bytes!!)
        } else if (file != null && mediaType != null) {
            // 文件
            return RequestBody.create(mediaType, file!!)
        } else if (fileParams.isNotEmpty()) {
            // 分片提交
            val multipartBodybuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            for (key in fileParams.keys) {
                val value = fileParams[key]
                if (value != null) {
                    val fileBody = RequestBody.create(guessMimeType(value.path), value)
                    multipartBodybuilder.addFormDataPart(key, value.name, fileBody)
                }
            }
            if (stringParams.isNotEmpty()) {
                for (key in stringParams.keys) {
                    val value = stringParams[key]
                    if (value != null) {
                        multipartBodybuilder.addFormDataPart(key, value)
                    }
                }
            }
            return multipartBodybuilder.build()
        } else if (stringParams.isNotEmpty()) {
            if (isMultipart) {
                // 强制分片
                val multipartBodybuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                for (key in stringParams.keys) {
                    val value = stringParams[key]
                    if (value != null) {
                        multipartBodybuilder.addFormDataPart(key, value)
                    }
                }
                return multipartBodybuilder.build()
            } else {
                // 默认表单
                val bodyBuilder = FormBody.Builder()
                for (key in stringParams.keys) {
                    val value = stringParams[key]
                    if (value != null) {
                        bodyBuilder.add(key, value)
                    }
                }
                return bodyBuilder.build()
            }
        } else {
            return null
        }
    }

    private fun guessMimeType(path: String): MediaType? {
        val result = path.replace("#", "")   //解决文件名中含有#号异常的问题
        val fileNameMap = URLConnection.getFileNameMap()
        var contentType = fileNameMap.getContentTypeFor(result)
        if (contentType == null) {
            contentType = MEDIA_TYPE_STREAM
        }
        return MediaType.parse(contentType)
    }
}