package com.xter.pickit.media

import android.content.Context
import android.provider.MediaStore
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.kit.L
import com.xter.pickit.kit.MimeUtil
import com.xter.pickit.kit.PlatformUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * @Author XTER
 * @Date 2021/11/29 9:41
 * @Description 本地资源载入
 */
class LocalMediaLoader {

    companion object {
        val INSTANCE = LocalMediaLoader();
    }

    private val FILE_SIZE_UNIT = 1024 * 1024L
    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC"
    private val NOT_GIF_UNKNOWN = "!='image/*'"
    private val NOT_GIF =
        " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/gif' AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF_UNKNOWN + ")"
    private val GROUP_BY_BUCKET_Id = " GROUP BY (bucket_id"
    private val COLUMN_COUNT = "count"
    private val COLUMN_BUCKET_ID = "bucket_id"
    private val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"

    private val fileSizeMax = 20 * 1024;
    private val fileSizeMin = 2;

    private val PROJECTION_29 = arrayOf(
        MediaStore.Files.FileColumns._ID,
        COLUMN_BUCKET_ID,
        COLUMN_BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE
    )

    private val PROJECTION = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.MediaColumns.DATA,
        COLUMN_BUCKET_ID,
        COLUMN_BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        "COUNT(*) AS " + COLUMN_COUNT
    )

    private fun getFileSizeCondition(): String? {
        return String.format(
            Locale.CHINA,
            "%d <%s " + MediaStore.MediaColumns.SIZE + " and " + MediaStore.MediaColumns.SIZE + " <= %d",
            Math.max(0, fileSizeMin),
            if (Math.max(0, fileSizeMin) == 0) "" else "=",
            fileSizeMax
        )
    }

    private fun getMimeCondition(): String? {
        val sb = StringBuilder()
        sb.append(" OR ").append(MediaStore.MediaColumns.MIME_TYPE).append("='")
            .append(MimeUtil.MIME_TYPE_PREFIX_IMAGE).append("'")
        return sb.toString()
    }

    private fun getSelectionArgsForImage(
        queryMimeTypeOptions: String?,
        fileSizeCondition: String?
    ): String? {
        val stringBuilder = StringBuilder()
        return if (PlatformUtil.isQ()) {
            stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeTypeOptions).toString()
        } else {
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeTypeOptions).append(")").append(")")
                .append(GROUP_BY_BUCKET_Id)
                .toString()
        }
    }

    private fun getSelection(): String? {
        val fileSizeCondition = getFileSizeCondition()
        val mimeCondition = getMimeCondition()
        return getSelectionArgsForImage(mimeCondition, fileSizeCondition)
    }

    fun loadImageFolders(context: Context, listener: IQueryResultListener<LocalMediaFolder>) {
        GlobalScope.launch {
            //TODO 可能不只需要封面，预留
            val selection = getSelection()
            L.d("selection is $selection")
            context.contentResolver?.query(
                QUERY_URI,
                if (PlatformUtil.isQ()) PROJECTION_29 else PROJECTION,
                selection,
                arrayOf("" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                if (PlatformUtil.isR()) ORDER_BY else null
            )?.let { cursor ->
                val count: Int = cursor.getCount()
                L.i("查询结果数量 count=$count")
                var totalCount = 0
                val mediaFolders: MutableList<LocalMediaFolder> = ArrayList<LocalMediaFolder>()
                if (count > 0) {
                    if (PlatformUtil.isQ()) {
                        //高于Android10只能遍历所有来统计各目录数量
                        val countMap: MutableMap<Long, Long> = HashMap()
                        while (cursor.moveToNext()) {
                            val bucketId: Long =
                                cursor.getLong(cursor.getColumnIndex(COLUMN_BUCKET_ID))
                            var newCount = countMap[bucketId]
                            if (newCount == null) {
                                newCount = 1L
                            } else {
                                newCount++
                            }
                            countMap[bucketId] = newCount
                        }
                        L.d("map=$countMap")
                        if (cursor.moveToFirst()) {
                            val hashSet: MutableSet<Long> = HashSet()
                            do {
                                val bucketId: Long =
                                    cursor.getLong(cursor.getColumnIndex(COLUMN_BUCKET_ID))
                                if (hashSet.contains(bucketId)) {
                                    continue
                                }
                                val bucketDisplayName: String? = cursor.getString(
                                    cursor.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME)
                                )
                                val mimeType: String? =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE))
                                val size = countMap[bucketId]!!
                                val id: Long =
                                    cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))

                                val mediaFolder = LocalMediaFolder().apply {
                                    setBucketId(bucketId)
                                    setName(bucketDisplayName)
                                    setImageNum(size.toInt())
                                    setFirstImagePath(
                                        MimeUtil.getRealPathUri(
                                            id,
                                            mimeType
                                        )
                                    )
                                    setFirstMimeType(mimeType)
                                }
                                mediaFolders.add(mediaFolder)
                                hashSet.add(bucketId)
                                totalCount += size.toInt()
                            } while (cursor.moveToNext())
                        }
                    } else {
                        cursor.moveToFirst()
                        do {
                            val bucketId: Long =
                                cursor.getLong(cursor.getColumnIndex(COLUMN_BUCKET_ID))
                            val bucketDisplayName: String? =
                                cursor.getString(cursor.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME))
                            val mimeType: String? =
                                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE))
                            val size: Int =
                                cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT))
                            val url: String? =
                                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))

                            val mediaFolder = LocalMediaFolder().apply {
                                setBucketId(bucketId)
                                setName(bucketDisplayName)
                                setImageNum(size)
                                setFirstImagePath(url)
                                setFirstMimeType(mimeType)
                            }
                            L.d("bucketId:$bucketId,bucketDisplayName:$bucketDisplayName")
                            L.i("url:$url")
                            mediaFolders.add(mediaFolder)
                            totalCount += size
                        } while (cursor.moveToNext())
                    }
                    L.i("total count=$totalCount")
                    listener.onCompleted(mediaFolders)
                }
            }
        }
    }
}