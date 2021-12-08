package com.xter.pickit.db

import android.content.Context
import androidx.room.Room
import com.xter.pickit.entity.LocalMediaGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author XTER
 * @Date 2021/12/8 16:00
 * @Description
 */
class RoomDBM private constructor() {
    companion object {
        fun get(): RoomDBM {
            return Holder.INSTANCE
        }
    }

    private object Holder {
        val INSTANCE = RoomDBM()
    }

    private var database: AppDatabase? = null

    fun init(context: Context) {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "pickit.db"
            ).build()
        }
    }

    suspend fun insertGroup(group: LocalMediaGroup): Long = withContext(Dispatchers.IO) {
        return@withContext database?.mediaDao()?.insertGroup(group)!!
    }
}