package com.xter.pickit.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * @Author XTER
 * @Date 2021/12/8 14:39
 * @Description 多对多关联表
 */
@Entity(tableName = "group_data_cross_ref",primaryKeys = ["group_id","id"])
data class GroupDataCrossRef(
    @ColumnInfo(name = "group_id") val groupId:Long,
    @ColumnInfo(name = "id") val mediaId:Long
)