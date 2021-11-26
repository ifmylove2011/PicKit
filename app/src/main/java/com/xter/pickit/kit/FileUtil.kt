package com.xter.pickit.kit

import android.text.TextUtils
import java.io.*

/**
 * Created by yh on 2018/6/11.
 * 文件的相关处理
 */
object FileUtil {
    /**
     * 删除文件或目录
     *
     * @param path 文件或目录。
     * @return true 表示删除成功，否则为失败
     */
    @Synchronized
    fun delete(path: File?): Boolean {
        if (null == path) {
            return true
        }
        if (path.isDirectory) {
            val files = path.listFiles()
            if (null != files) {
                for (file in files) {
                    if (!delete(file)) {
                        return false
                    }
                }
            }
        }
        return !path.exists() || path.delete()
    }

    @Throws(IOException::class)
    fun copyfile(url1: String?, url2: String?) {
        val fis = FileInputStream(url1)
        val bufis = BufferedInputStream(fis)
        val fos = FileOutputStream(url2)
        val bufos = BufferedOutputStream(fos)
        var len = 0
        while (bufis.read().also { len = it } != -1) {
            bufos.write(len)
        }
        bufis.close()
        bufos.close()
    }

    /**
     * 备份文件
     *
     * @param path 原路径
     */
    @Synchronized
    fun backupFile(path: String) {
        try {
            copyfile(path, "$path.bak")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 备份文件
     *
     * @param path 原路径
     */
    @Synchronized
    fun revertFile(path: String) {
        try {
            copyfile("$path.bak", path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    fun createFile(path: String) {
        if (TextUtils.isEmpty(path)) {
            return
        }
        val file = File(path)
        if (!file.exists()) if (path.endsWith("/")) {
            file.mkdirs()
        } else {
            if (file.parentFile.exists()) {
                file.createNewFile()
            } else {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
        }
    }

    @Synchronized
    fun writeToFile(
        bytes: ByteArray?,
        file: File?,
        append: Boolean
    ) {
        var fo: FileOutputStream? = null
        try {
            fo = FileOutputStream(file, append)
            fo.write(bytes)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fo?.close()
        }
    }

    @Throws(IOException::class)
    fun readCurrentFile(file: File?): String {
        val input: InputStream = FileInputStream(file)
        return try {
            val reader = BufferedReader(
                InputStreamReader(
                    input
                )
            )
            val sb = StringBuilder()
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            sb.toString()
        } finally {
            input.close()
        }
    }

}