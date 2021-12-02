package com.xter.pickit.entity

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

/**
 * @author：luck
 * @date：2017-5-24 16:21
 * @describe：Media Entity
 * [
](https://github.com/LuckSiege/PictureSelector/wiki/PictureSelector-%E8%B7%AF%E5%BE%84%E8%AF%B4%E6%98%8E) */
data class LocalMedia(
    var id: Long,
    var bucketId: Long,
    var name: String?,
    var parentName: String?,
    var path: String?,
    var realPath: String?,
    var duration: Long,
    var mimeType: String?,
    var width:Int,
    var height:Int,
    var size: Long,
    var dateAddedTime: Long,
    var dateModifiedTime: Long
) : Parcelable {

    var originalPath : String? = null

    var androidQToPath: String? = null

    var isSelected = false

    var position = 0

    var orientation = -1

    /**
     * isLongImage
     * # For internal use only
     */
    var isLongImage = false

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong()
    ) {
        originalPath = parcel.readString()
        androidQToPath = parcel.readString()
        isSelected = parcel.readByte() != 0.toByte()
        position = parcel.readInt()
        orientation = parcel.readInt()
        isLongImage = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(bucketId)
        parcel.writeString(name)
        parcel.writeString(parentName)
        parcel.writeString(path)
        parcel.writeString(realPath)
        parcel.writeLong(duration)
        parcel.writeString(mimeType)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeLong(size)
        parcel.writeLong(dateAddedTime)
        parcel.writeLong(dateModifiedTime)
        parcel.writeString(originalPath)
        parcel.writeString(androidQToPath)
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeInt(position)
        parcel.writeInt(orientation)
        parcel.writeByte(if (isLongImage) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocalMedia> {
        override fun createFromParcel(parcel: Parcel): LocalMedia {
            return LocalMedia(parcel)
        }

        override fun newArray(size: Int): Array<LocalMedia?> {
            return arrayOfNulls(size)
        }
    }

}