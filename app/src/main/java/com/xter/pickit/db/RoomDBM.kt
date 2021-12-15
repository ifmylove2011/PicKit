package com.xter.pickit.db

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xter.pickit.entity.GroupDataCrossRef
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaGroup
import com.xter.pickit.entity.MediaGroupWithData
import com.xter.pickit.kit.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author XTER
 * @Date 2021/12/8 16:00
 * @Description 数据管理
 */
class RoomDBM private constructor() {
    companion object {
        fun get(): RoomDBM {
            return Holder.INSTANCE
        }
    }

    @VisibleForTesting
    val DATABASE_NAME = "pickit"

    private object Holder {
        val INSTANCE = RoomDBM()
    }

    private var database: AppDatabase? = null

    fun init(context: Context) {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, DATABASE_NAME
            ).addMigrations(MIGRATION_1_2)
                .build()
        }
    }

    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            L.w("MIGRATION_1_2 begin")
            database.execSQL("CREATE TABLE IF NOT EXISTS `media_new` (`id` INTEGER NOT NULL, `bucket_id` INTEGER NOT NULL, `name` TEXT, `parent_name` TEXT, `path` TEXT, `real_path` TEXT, `duration` INTEGER NOT NULL, `mime_type` TEXT, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, `size` INTEGER NOT NULL, `date_added` INTEGER NOT NULL, `date_modified` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL(
                "INSERT INTO `media_new` (`id`,`bucket_id`,`name`,`parent_name`,`path`,`real_path`,`duration`,`mime_type`,`width`,`height`,`size`,`date_added`,`date_modified`) SELECT `id`,`bucket_id`,`name`,`parent_name`,`path`,`real_path`,`duration`,`mime_type`,`width`,`height`,`size`,`date_added`,`date_modified` FROM `media`"
            )
            database.execSQL("DROP TABLE `media`")
            database.execSQL("ALTER TABLE `media_new` RENAME TO `media`")
            L.w("MIGRATION_1_2 end")
        }
    }

    suspend fun insertGroup(group: LocalMediaGroup): Long = withContext(Dispatchers.IO) {
        return@withContext database?.mediaDao()?.insertGroup(group)!!
    }

    suspend fun updateGroup(group: LocalMediaGroup): Int = withContext(Dispatchers.IO) {
        return@withContext database?.mediaDao()?.updateGroup(group)!!
    }

    suspend fun queryGroup(): List<LocalMediaGroup>? = withContext(Dispatchers.IO) {
        return@withContext database?.mediaDao()?.queryGroup()
    }

    suspend fun getGroupWithData(): List<MediaGroupWithData>? = withContext(Dispatchers.IO) {
        return@withContext database?.mediaDao()?.getGroupWithData()
    }

    suspend fun getGroupWithData(groupId: Long): MediaGroupWithData? = withContext(Dispatchers.IO) {
        return@withContext database?.mediaDao()?.getGroupWithData(groupId)
    }

    suspend fun deleteGroups(groups: List<LocalMediaGroup>): Int = withContext(Dispatchers.IO) {
        val size = database?.mediaDao()?.deleteGroups(groups)
        groups.map {
            it.groupId
        }.let { groupIds ->
            val groupIdArray = groupIds.toTypedArray()
            L.i(groupIdArray.contentToString())
            val refSize = database?.mediaDao()?.deleteGroupCrossRef(groupIdArray)
            if (refSize != 0) {
                val rows = database?.mediaDao()?.deleteUnuseMediaData()
                L.i("delete LocalMedia num = $rows")
            }
        }
        return@withContext size!!
    }

    /**
     * 1.存储LocalMedia
     * 2.建立级联实体，存储相关级联表
     * 3.更新上级数据
     */
    suspend fun saveMediaDataWithCrossRef(group: LocalMediaGroup, data: List<LocalMedia>) =
        withContext(Dispatchers.IO) {
            database?.mediaDao()?.insertMediaData(data)?.let { idArray ->
                L.i("save LocalMedia num ${idArray.size}")
                val crossRefList = mutableListOf<GroupDataCrossRef>()
                idArray.forEach { id ->
                    crossRefList.add(GroupDataCrossRef(group.groupId, id))
                }
                //存储级联表，更新group
                database?.mediaDao()?.insertGroupCrossRef(crossRefList)?.let { refIdArray ->
                    L.i("save GroupDataCrossRef num ${refIdArray.size}")
                    group.imageNum = group.imageNum + refIdArray.size
                    database?.mediaDao()?.updateGroup(group)
                }
            }
        }

    suspend fun checkGroup(group: LocalMediaGroup) {

    }

    /**
     * 主要是删除未被引用的LocalMedia
     */
    suspend fun checkMediaData() {

    }
}