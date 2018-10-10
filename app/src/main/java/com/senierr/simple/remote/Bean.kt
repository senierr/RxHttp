package com.senierr.simple.remote

/**
 * 实体
 *
 * @author zhouchunjie
 * @date 2018/5/2
 */

/***************************** Bmob实体 ********************************/
data class BmobError(
        var code: Int,
        var error: String? = null
) : Exception(error)

data class BmobInsert(
        var objectId: String,
        var createdAt: String
)

data class BmobUpdate(
        var updatedAt: String
)

data class BmobDelete(
        var msg: String     // ok/fail
)

data class BmobArray<T>(
        var results: MutableList<T>? = null,
        var count: Int = 0
)

data class BmobFile(
        var filename: String,
        var url: String,
        var cdn: String
)

/***************************** 自定义实体 ********************************/
data class Note(
        var objectId: String? = null,
        var createdAt: String? = null,
        var updatedAt: String? = null,

        var content: String? = null
)

data class CloudFile(
        var objectId: String? = null,
        var createdAt: String? = null,
        var updatedAt: String? = null,

        var filename: String? = null,
        var url: String? = null,
        var cdn: String? = null
)

data class DownloadProgress(
        var url: String,
        val destPath: String,
        val totalSize: Long,
        val currentSize: Long,
        val percent: Int
)