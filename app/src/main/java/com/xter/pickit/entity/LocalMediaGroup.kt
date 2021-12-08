package com.xter.pickit.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author：XTER
 * @date：2021-11-28 15:21
 * @describe：分组实体
 */
@Entity(tableName = "group")
data class LocalMediaGroup(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "group_id") var groupId: Long,
    var name: String?,
    @ColumnInfo(name = "first_image_path") var firstImagePath: String?,
    @ColumnInfo(name = "first_mime_type") var firstMimeType: String?,
    @ColumnInfo(name = "image_num") var imageNum: Int,
) : Parcelable {

    @Ignore var coverData: List<LocalMedia>? = ArrayList()

    @Ignore var selected: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
        coverData = parcel.createTypedArrayList(LocalMedia)
        selected = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(groupId)
        parcel.writeString(name)
        parcel.writeString(firstImagePath)
        parcel.writeString(firstMimeType)
        parcel.writeInt(imageNum)
        parcel.writeTypedList(coverData)
        parcel.writeByte(if (selected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocalMediaGroup> {
        override fun createFromParcel(parcel: Parcel): LocalMediaGroup {
            return LocalMediaGroup(parcel)
        }

        override fun newArray(size: Int): Array<LocalMediaGroup?> {
            return arrayOfNulls(size)
        }
    }

}