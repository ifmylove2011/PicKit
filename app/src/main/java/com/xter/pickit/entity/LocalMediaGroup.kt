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

    override fun toString(): String {
        return "LocalMediaGroup(groupId=$groupId, name=$name, firstImagePath=$firstImagePath, firstMimeType=$firstMimeType, imageNum=$imageNum, selected=$selected)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalMediaGroup

        if (groupId != other.groupId) return false
        if (name != other.name) return false
        if (firstImagePath != other.firstImagePath) return false
        if (firstMimeType != other.firstMimeType) return false
        if (imageNum != other.imageNum) return false
        if (selected != other.selected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupId.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (firstImagePath?.hashCode() ?: 0)
        result = 31 * result + (firstMimeType?.hashCode() ?: 0)
        result = 31 * result + imageNum
        result = 31 * result + selected.hashCode()
        return result
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