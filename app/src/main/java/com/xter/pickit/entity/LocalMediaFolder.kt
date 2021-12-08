package com.xter.pickit.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author：XTER
 * @date：2021-11-28 15:21
 * @describe：MediaFolder Entity
 */
data class LocalMediaFolder(
    var bucketId: Long,
    var name: String?,
    var firstImagePath: String?,
    var firstMimeType: String?,
     var imageNum: Int,
) : Parcelable {

    /**
     * data
     */
    var data: List<LocalMedia>? = ArrayList()

    var selected: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
        data = parcel.createTypedArrayList(LocalMedia.CREATOR)
        selected = parcel.readByte() != 0.toByte()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalMediaFolder

        if (bucketId != other.bucketId) return false

        return true
    }

    override fun hashCode(): Int {
        return bucketId.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(bucketId)
        parcel.writeString(name)
        parcel.writeString(firstImagePath)
        parcel.writeString(firstMimeType)
        parcel.writeInt(imageNum)
        parcel.writeTypedList(data)
        parcel.writeByte(if (selected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocalMediaFolder> {
        override fun createFromParcel(parcel: Parcel): LocalMediaFolder {
            return LocalMediaFolder(parcel)
        }

        override fun newArray(size: Int): Array<LocalMediaFolder?> {
            return arrayOfNulls(size)
        }
    }


}