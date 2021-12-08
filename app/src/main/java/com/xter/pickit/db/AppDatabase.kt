package com.xter.pickit.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xter.pickit.entity.GroupDataCrossRef
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaGroup

/**
 * @Author XTER
 * @Date 2021/12/8 14:15
 * @Description ROOM数据库
 */
@Database(entities = [LocalMediaGroup::class, LocalMedia::class, GroupDataCrossRef::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
}