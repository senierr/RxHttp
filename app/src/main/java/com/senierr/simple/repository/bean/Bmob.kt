package com.senierr.simple.repository.bean

/**
 * Bmob对象
 *
 * @author zhouchunjie
 * @date 2018/5/2
 */
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

data class BmobServerData(
        var timestamp: Long,
        var datetime: String
)