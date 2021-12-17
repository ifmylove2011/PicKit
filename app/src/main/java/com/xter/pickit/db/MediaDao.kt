package com.xter.pickit.db

import androidx.room.*
import com.xter.pickit.entity.*

/**
 * @Author XTER
 * @Date 2021/12/8 14:16
 * @Description
 */
@Dao
interface MediaDao {

    /*------------  LocalMediaGroup ------------*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(group: LocalMediaGroup): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateGroup(group: LocalMediaGroup): Int

    /**
     * TODO　并非单纯的删除分组，要考虑删除对应关系
     */
    @Delete
    fun deleteGroup(group: LocalMediaGroup): Int

    @Delete
    fun deleteGroups(groups: List<LocalMediaGroup>): Int

    @Query("SELECT * FROM `group` ORDER BY `date_modified` DESC")
    fun queryGroup(): List<LocalMediaGroup>?

    /*------------  MediaGroupWithData ------------*/

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM `group`")
    fun getGroupWithData(): List<MediaGroupWithData>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM `group` WHERE group_id = :groupId")
    fun getGroupWithData(groupId: Long): MediaGroupWithData

    /*------------  LocalMedia ------------*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMediaData(data: LocalMedia): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMediaData(data: List<LocalMedia>): LongArray

    @Delete
    fun deleteMediaData(data: LocalMedia): Int

    @Delete
    fun deleteMediaData(data: List<LocalMedia>): Int

    @Query("DELETE FROM `media` WHERE id NOT IN (SELECT id FROM group_data_cross_ref)")
    fun deleteUnuseMediaData(): Int

    @Query("SELECT * FROM `media` WHERE id IN (SELECT id FROM group_data_cross_ref WHERE group_id = :groupId) ORDER BY date_lasted_view DESC,date_modified DESC LIMIT 1")
    fun queryLastedMediaData(groupId: Long): LocalMedia

    @Query("SELECT * FROM `media` WHERE id IN (SELECT id FROM group_data_cross_ref WHERE group_id = :groupId) ORDER BY date_lasted_view DESC,date_modified DESC LIMIT :limit")
    fun queryLastedMoreMediaData(groupId: Long, limit: Int): List<LocalMedia>

    /*------------  GroupDataCrossRef ------------*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupCrossRef(data: GroupDataCrossRef): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupCrossRef(data: List<GroupDataCrossRef>): LongArray

    @Delete
    fun deleteGroupCrossRef(data: List<GroupDataCrossRef>): Int

    @Query("DELETE FROM group_data_cross_ref WHERE group_id = :groupId")
    fun deleteGroupCrossRef(groupId: Long): Int

    @Query("DELETE FROM group_data_cross_ref WHERE group_id IN (:groupIds)")
    fun deleteGroupCrossRef(groupIds: Array<Long>): Int

    @Query("DELETE FROM group_data_cross_ref WHERE group_id = (:groupId) AND id IN (:mediaIds)")
    fun deleteMediaCrossRef(groupId: Long, mediaIds: Array<Long>): Int
}