package com.xter.pickit.entity

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.xter.pickit.kit.DateTimeUtils

/**
 * @author：luck
 * @date：2017-5-24 16:21
 * @describe：Media Entity
 * [
](https://github.com/LuckSiege/PictureSelector/wiki/PictureSelector-%E8%B7%AF%E5%BE%84%E8%AF%B4%E6%98%8E) */
@Entity(tableName = "media")
data class LocalMedia(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "bucket_id") var bucketId: Long,
    var name: String?,
    @ColumnInfo(name = "parent_name") var parentName: String?,
    var path: String?,
    @ColumnInfo(name = "real_path") var realPath: String?,
    var duration: Long,
    @ColumnInfo(name = "mime_type") var mimeType: String?,
    var width: Int,
    var height: Int,
    var size: Long,
    @ColumnInfo(name = "date_added") var dateAddedTime: Long,
    @ColumnInfo(name = "date_modified") var dateModifiedTime: Long
) : Parcelable {

    @ColumnInfo(name = "date_lasted_view")
    var lastedViewTime: Long = 0

    @Ignore
    var originalPath: String? = null

    @Ignore
    var androidQToPath: String? = null

    @Ignore
    var isSelected = false

    @Ignore
    var position = 0

    @Ignore
    var orientation = -1

    /**
     * isLongImage
     * # For internal use only
     */
    @Ignore
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
        lastedViewTime = parcel.readLong()
        originalPath = parcel.readString()
        androidQToPath = parcel.readString()
        isSelected = parcel.readByte() != 0.toByte()
        position = parcel.readInt()
        orientation = parcel.readInt()
        isLongImage = parcel.readByte() != 0.toByte()
    }

    fun getFormatSize(): String {
        if (size / (1024 * 1024) > 1) {
            return String.format("%.2fMb", size.toFloat() / (1024 * 1024))
        } else if (size / 1024 > 1) {
            return String.format("%.2fKb", size.toFloat() / 1024)
        } else {
            return String.format("%db", size)
        }
    }

    fun getFormatAddTime(): String {
        return DateTimeUtils.getNormalDate1(dateAddedTime * 1000)
    }

    fun getFormatModifiedTime(): String {
        return DateTimeUtils.getNormalDate1(dateModifiedTime * 1000)
    }

    fun getFormatLastedViewTime(): String {
        return DateTimeUtils.getNormalDate1(lastedViewTime * 1000)
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
        parcel.writeLong(lastedViewTime)
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