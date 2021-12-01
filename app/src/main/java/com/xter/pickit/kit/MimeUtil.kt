package com.xter.pickit.kit

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File

/**
 * @Author XTER
 * @Date 2021/11/29 10:26
 * @Description
 */
object MimeUtil {
    private const val MIME_TYPE_PNG = "image/png"
    private const val MIME_TYPE_JPEG = "image/jpeg"
    private const val MIME_TYPE_JPG = "image/jpg"
    private const val MIME_TYPE_BMP = "image/bmp"
    private const val MIME_TYPE_GIF = "image/gif"
    private const val MIME_TYPE_WEBP = "image/webp"

    private const val MIME_TYPE_3GP = "video/3gp"
    private const val MIME_TYPE_MP4 = "video/mp4"
    private const val MIME_TYPE_MPEG = "video/mpeg"
    private const val MIME_TYPE_AVI = "video/avi"

    const val JPEG = ".jpeg"

    const val JPG = ".jpg"

    const val PNG = ".png"

    const val WEBP = ".webp"

    const val GIF = ".gif"

    const val BMP = ".bmp"

    const val AMR = ".amr"

    const val WAV = ".wav"

    const val MP3 = ".mp3"

    const val MP4 = ".mp4"

    const val AVI = ".avi"

    const val JPEG_Q = "image/jpeg"

    const val PNG_Q = "image/png"

    const val MP4_Q = "video/mp4"

    const val AVI_Q = "video/avi"

    const val AMR_Q = "audio/amr"

    const val WAV_Q = "audio/x-wav"

    const val MP3_Q = "audio/mpeg"

    const val DCIM = "DCIM/Camera"

    const val CAMERA = "Camera"

    const val MIME_TYPE_IMAGE = "image/jpeg"
    const val MIME_TYPE_VIDEO = "video/mp4"
    const val MIME_TYPE_AUDIO = "audio/mpeg"
    const val MIME_TYPE_AUDIO_AMR = "audio/amr"

    const val MIME_TYPE_PREFIX_IMAGE = "image"
    const val MIME_TYPE_PREFIX_VIDEO = "video"
    const val MIME_TYPE_PREFIX_AUDIO = "audio"

    fun isHasImage(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_IMAGE)
    }

    fun isHasVideo(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_VIDEO)
    }

    fun isHasAudio(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_AUDIO)
    }

    fun getRealPathUri(id: Long, mimeType: String?): String? {
        val contentUri: Uri
        contentUri = if (isHasImage(mimeType)) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if (isHasVideo(mimeType)) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else if (isHasAudio(mimeType)) {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        return ContentUris.withAppendedId(contentUri, id).toString()
    }

    fun getImageMimeType(path: String?): String? {
        try {
            if (!TextUtils.isEmpty(path)) {
                val file = File(path)
                val fileName = file.name
                val beginIndex = fileName.lastIndexOf(".")
                val temp = if (beginIndex == -1) "jpeg" else fileName.substring(beginIndex + 1)
                return "image/$temp"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return MIME_TYPE_IMAGE
        }
        return MIME_TYPE_IMAGE
    }
}