package com.xter.pickit.db

import androidx.room.*
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.entity.LocalMediaGroup
import com.xter.pickit.entity.MediaGroupWithData

/**
 * @Author XTER
 * @Date 2021/12/8 14:16
 * @Description
 */
@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(group: LocalMediaGroup): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateGroup(group: LocalMediaGroup):Int

    /**
     * TODO　并非单纯的删除分组，要考虑删除对应关系
     */
    @Delete
    fun deleteGroup(group: LocalMediaGroup) :Int

    @Query("SELECT * FROM `group`")
    fun queryGroup():List<LocalMediaGroup>?

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM `group`")
    fun getGroupWithData(): List<MediaGroupWithData>

    @Insert
    fun insertMediaData(data:LocalMedia)

    /**
     * TODO　并非单纯的删除数据，要考虑删除对应关系
     */
    @Delete
    fun deleteMediaData(data:LocalMedia)


}