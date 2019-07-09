package com.senierr.http.progress

/**
 * 进度
 *
 * @author zhouchunjie
 * @date 2019/7/9
 */
data class Progress (
        val tag: String,        // 标签
        val totalSize: Long,    // 总大小
        val currentSize: Long,  // 已下载大小
        val percent: Int        // 进度 0~100
)