package com.senierr.simple.repository.bean

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * 笔记实体
 *
 * @author zhouchunjie
 * @date 2018/9/22
 */
@Entity(tableName = "Note")
data class Note(
        @PrimaryKey
        var objectId: String,
        var createdAt: String? = null,
        var updatedAt: String? = null,

        var title: String? = null,
        var content: String? = null
)