package com.xter.pickit.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * @Author XTER
 * @Date 2021/12/8 14:31
 * @Description 多对多关联之下，目录带数据的结构实体
 */
data class MediaGroupWithData(
    @Embedded val group: LocalMediaGroup,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "id",
        associateBy = Junction(GroupDataCrossRef::class)
    )
    val mediaData: List<LocalMedia>
)
