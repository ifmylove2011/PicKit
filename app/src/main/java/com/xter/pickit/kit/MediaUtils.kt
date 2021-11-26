package com.xter.pickit.kit

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

/**
 * @Author XTER
 * @Date 2021/11/26 15:00
 * @Description 媒体相关工具，主要用于获取图片信息
 */
object MediaUtils {

    fun queryImages(context: Context) {
        val imageDataUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.MIME_TYPE
        )
        context.contentResolver.query(imageDataUri, projection, null, null, null)
            ?.let { cursor ->

                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val typeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    val path = cursor.getString(dataIndex)

                    if(cursor.isFirst){
                        L.d(imageUri.toString())
                    }
                }
                cursor.close()
            }

    }
}