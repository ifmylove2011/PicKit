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
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
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

    private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            L.w("MIGRATION_2_3 begin")
            database.execSQL("ALTER TABLE `group` ADD COLUMN `date_added` INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE `group` ADD COLUMN `date_modified` INTEGER NOT NULL DEFAULT 0")
            L.w("MIGRATION_2_3 end")
        }
    }

    private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            L.w("MIGRATION_3_4 begin")
            database.execSQL("ALTER TABLE `media` ADD COLUMN `date_lasted_view` INTEGER NOT NULL DEFAULT 0")
            L.w("MIGRATION_3_4 end")
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

    suspend fun queryGroup(coverNum: Int): List<LocalMediaGroup>? = withContext(Dispatchers.IO) {
        val data = database?.mediaDao()?.queryGroup()?.onEach { group ->
            group.coverData =
                database?.mediaDao()?.queryLastedMoreMediaData(group.groupId, coverNum)
        }
        return@withContext data
    }

    suspend fun getGroupWithData(): List<MediaGroupWithData>? = withContext(Dispatchers.IO) {
        return@withContext database?.mediaDao()?.getGroupWithData()
    }

    suspend fun getGroupWithData(groupId: Long): MediaGroupWithData? = withContext(Dispatchers.IO) {
        return@withContext database?.mediaDao()?.getGroupWithData(groupId)
    }

    /**
     * 1.删除group本体
     * 2.删除级联实体GroupCrossRef
     * 3.删除未引用的LocalMedia
     */
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
                L.i("delete unused LocalMedia num = $rows")
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
                    updateGroupState(group, refIdArray.size)
                }
            }
        }

    /**
     * 1.删除级联实体数据
     * 2.删除未引用的LocalMedia
     * 3.更新上级数据
     */
    suspend fun deleteMediaData(group: LocalMediaGroup, data: List<LocalMedia>) =
        withContext(Dispatchers.IO) {
            data.map {
                it.id
            }.let { mediaIds ->
                val mediaIdArray = mediaIds.toTypedArray()
                L.i(mediaIdArray.contentToString())
                val refSize = database?.mediaDao()?.deleteMediaCrossRef(group.groupId, mediaIdArray)
                if (refSize != 0) {
                    val rows = database?.mediaDao()?.deleteUnuseMediaData()
                    L.i("delete unused LocalMedia num = $rows")
                    updateGroupState(group, -refSize!!)
                }
            }
        }

    /**
     * 更新group信息，包括修改时间与封面检测改动
     */
    suspend fun updateGroupState(group: LocalMediaGroup, changeNum: Int) {
        group.imageNum += changeNum
        if (group.dateAddedTime == 0L) {
            group.dateAddedTime = System.currentTimeMillis() / 1000
        }
        group.dateModifiedTime = System.currentTimeMillis() / 1000
        database?.mediaDao()?.queryLastedMediaData(group.groupId)?.let { localMedia ->
            group.firstImagePath = localMedia.path
            group.firstMimeType = localMedia.mimeType
        }
        database?.mediaDao()?.updateGroup(group)
    }
}