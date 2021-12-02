package com.xter.pickit.media

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.kit.L
import com.xter.pickit.kit.MimeUtil
import com.xter.pickit.kit.PlatformUtil
import kotlinx.coroutines.*
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
    private val ORDER_BY = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
    private val NOT_GIF_UNKNOWN = "!='image/*'"
    private val NOT_GIF =
        " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/gif' AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF_UNKNOWN + ")"
    private val GROUP_BY_BUCKET_Id = " GROUP BY (bucket_id"
    private val COLUMN_COUNT = "count"
    private val COLUMN_BUCKET_ID = "bucket_id"
    private val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"

    private val fileSizeMax = 20 * 1024
    private val fileSizeMin = 2
    private val pageMaxSize = 60
    private var mPage = 1

    private val PROJECTION_BUCKET_29 = arrayOf(
        MediaStore.Files.FileColumns._ID,
        COLUMN_BUCKET_ID,
        COLUMN_BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.DATE_MODIFIED
    )

    private val PROJECTION_BUCKET = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.MediaColumns.DATA,
        COLUMN_BUCKET_ID,
        COLUMN_BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.DATE_MODIFIED,
        "COUNT(*) AS " + COLUMN_COUNT
    )

    private val PROJECTION_PAGE = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT,
        MediaStore.MediaColumns.DURATION,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.DISPLAY_NAME,
        COLUMN_BUCKET_ID,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.DATE_MODIFIED
    )

    private fun getFileSizeCondition(): String {
        return String.format(
            Locale.CHINA,
            "%d <%s " + MediaStore.MediaColumns.SIZE + " and " + MediaStore.MediaColumns.SIZE + " <= %d",
            Math.max(0, fileSizeMin),
            if (Math.max(0, fileSizeMin) == 0) "" else "=",
            fileSizeMax
        )
    }

    private fun getMimeCondition(): String {
        val sb = StringBuilder()
        sb.append(" OR ").append(MediaStore.MediaColumns.MIME_TYPE).append("='")
            .append(MimeUtil.MIME_TYPE_PREFIX_IMAGE).append("'")
        return sb.toString()
    }

    private fun getSelectionArgsForImage(
        queryMimeTypeOptions: String?
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

    private fun getSelectionForFolder(): String? {
//        val fileSizeCondition = getFileSizeCondition()
        val mimeCondition = getMimeCondition()
        return getSelectionArgsForImage(mimeCondition)
    }

    private fun getPageSelection(bucketId: Long): String {
        val mimeCondition = getMimeCondition()
        return getPageSelectionArgsForImage(bucketId, mimeCondition)
    }

    private fun getSelectionArgsForPageSingleMediaType(
        mediaType: Int,
        bucketId: Long
    ): Array<String> {
        return if (bucketId == -1L) arrayOf(mediaType.toString()) else arrayOf(
            mediaType.toString(),
            bucketId.toString()
        )
    }

    private fun getPageSelectionArgs(bucketId: Long): Array<String> {
        // Get photo
        return getSelectionArgsForPageSingleMediaType(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
            bucketId
        )
    }

    private fun getPageSelectionArgsForImage(
        bucketId: Long,
        queryMimeCondition: String
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
        return if (bucketId == -1L) {
            stringBuilder.append(queryMimeCondition).append(")").toString()
        } else {
            stringBuilder.append(queryMimeCondition).append(") AND ")
                .append(COLUMN_BUCKET_ID)
                .append("=? ").toString()
        }
    }

    fun createQueryArgsBundle(
        selection: String,
        selectionArgs: Array<String>,
        limitCount: Int,
        offset: Int
    ): Bundle {
        val queryArgs = Bundle()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            queryArgs.putString(
                ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                MediaStore.Files.FileColumns._ID + " DESC"
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                queryArgs.putString(
                    ContentResolver.QUERY_ARG_SQL_LIMIT,
                    "$limitCount offset $offset"
                )
            }
        }
        return queryArgs
    }

    suspend fun loadImageFolders(
        context: Context,
        listener: IQueryResultListener<LocalMediaFolder>
    ) = withContext(Dispatchers.IO) {
        //TODO 可能不只需要封面，预留
        val selection = getSelectionForFolder()
        L.d("selection is $selection")
        context.contentResolver?.query(
            QUERY_URI,
            if (PlatformUtil.isQ()) PROJECTION_BUCKET_29 else PROJECTION_BUCKET,
            selection,
            arrayOf("" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            if (PlatformUtil.isR()) null else ORDER_BY
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

                            val mediaFolder = LocalMediaFolder(
                                bucketId, bucketDisplayName, MimeUtil.getRealPathUri(
                                    id,
                                    mimeType
                                ), mimeType, size.toInt()
                            )
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
                        val dateModified =
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED))
                        val mediaFolder = LocalMediaFolder(
                            bucketId, bucketDisplayName, url, mimeType, size
                        )
//                            L.d("bucketId:$bucketId,bucketDisplayName:$bucketDisplayName")
//                            L.i("url:$url")
//                            L.i("date:$dateModified")
                        mediaFolders.add(mediaFolder)
                        totalCount += size
                    } while (cursor.moveToNext())
                }
                L.i("total count=$totalCount")
                listener.onCompleted(mediaFolders)
            }
            cursor.close()
        }
    }

    /**
     * 根据目录查询图片信息
     * @param context Context
     * @param bucketId 目录ID
     * @param size 查询数量，Android11开始分区存储，需要带入limit进行查询
     * @param listener 结果监听
     */
    suspend fun loadImages(
        context: Context,
        bucketId: Long,
        size: Int,
        listener: IQueryResultListener<LocalMedia>
    ) = withContext(Dispatchers.IO) {
        var cursor: Cursor? = null
        val pageSelection = getPageSelection(bucketId)
        val pageSelectionArgs = getPageSelectionArgs(bucketId)
        L.i("selection=$pageSelection")
        L.i("selectionArgs=${Arrays.toString(pageSelectionArgs)}")
        if (PlatformUtil.isR()) {
            val queryArgs: Bundle = createQueryArgsBundle(
                pageSelection,
                pageSelectionArgs,
                pageMaxSize,
                pageMaxSize
            )
            cursor = context.contentResolver.query(QUERY_URI, PROJECTION_PAGE, queryArgs, null)
        } else {
            //TODO 排序需要多个选择
            val orderBy =
                if (mPage == -1) MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC" else MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC limit " + size
            cursor = context.contentResolver.query(
                QUERY_URI,
                PROJECTION_PAGE,
                pageSelection,
                pageSelectionArgs,
                orderBy
            )
        }
        cursor?.let { cursor ->
            val count = cursor.count
            L.i("image size = $count")
            val localMediaData: MutableList<LocalMedia> = ArrayList<LocalMedia>()
            if (count > 0) {
                val idColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(0))
                val dataColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(1))
                val mimeTypeColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(2))
                val widthColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(3))
                val heightColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(4))
                val durationColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(5))
                val sizeColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(6))
                val folderNameColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(7))
                val fileNameColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(8))
                val bucketIdColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(9))
                val dateAddedColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(10))
                val dateModifiedColumn: Int = cursor.getColumnIndexOrThrow(PROJECTION_PAGE.get(11))
                cursor.moveToFirst()

                do {
                    val id = cursor.getLong(idColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val absolutePath = cursor.getString(dataColumn)
                    val url = if (PlatformUtil.isQ()) MimeUtil.getRealPathUri(
                        id,
                        mimeType
                    ) else absolutePath
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val folderName = cursor.getString(folderNameColumn)
                    val fileName = cursor.getString(fileNameColumn)
                    val bucketId = cursor.getLong(bucketIdColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val dateModified = cursor.getLong(dateModifiedColumn)

                    val localMedia = LocalMedia(
                        id,
                        bucketId,
                        fileName,
                        folderName,
                        url,
                        absolutePath,
                        duration,
                        mimeType,
                        width,
                        height,
                        size,
                        dateAdded,
                        dateModified
                    )
                    localMediaData.add(localMedia)

//                        L.d(localMedia.toString())
                } while (cursor.moveToNext())

                listener.onCompleted(localMediaData)
            } else {
                L.w("未查询到数据，目录ID=$bucketId")
            }
        }
    }

}