package com.xter.pickit.kit

import android.os.Environment
import com.xter.pickit.kit.FileUtil.createFile

import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author XTER
 * @date 2019/6/4
 * 日志写入工具
 */
class LogFileWriter private constructor() {

    private val ioThread: ExecutorService

    private val buffer: BlockingQueue<String>

    private var recent: Long = 0
    private var tomorrow: Long = 0

    private var fileName: String? = null

    private val fileIndex: AtomicInteger

    private val tomorrowMills: Long
        get() = System.currentTimeMillis() - 8 * 3600 * 1000 + (24 * 3600 * 1000 - System.currentTimeMillis() % (24 * 3600 * 1000))

    private val FILE_MAX_SIZE = 2*1024*1024;

    private object Holder {
        val INSTANCE = LogFileWriter()
    }

    init {
        ioThread = ThreadPoolExecutor(
            1, 1,
            0L, TimeUnit.MILLISECONDS,
            ArrayBlockingQueue(2)
        )
        buffer = ArrayBlockingQueue(BUFFER_SIZE)
        recent = System.currentTimeMillis()
        fileIndex = AtomicInteger(checkFileStartIndex())
        fileName = String.format(
            Locale.CHINA,
            "%s%s_%02d.log",
            FILE_PREFIX,
            DATE_FORMAT.format(recent),
            fileIndex.get()
        )
        println(fileName)
        tomorrow = tomorrowMills
        flushForever()
    }

    private fun checkFileStartIndex(): Int {
        val logDir = File(FILE_PREFIX)
        val filePrefix = DATE_FORMAT.format(recent)
        val fileNames =
            logDir.list { dir, name -> name.startsWith(filePrefix) }
        return checkFileMaxIndex(fileNames)
    }

    private fun checkFileMaxIndex(names: Array<String>?): Int {
        var maxIndex = 1
        if (names != null && names.size > 1) {
            val len = names.size
            var maxFileIndex = names[0]
            for (i in 1 until len) {
                if (names[i].compareTo(maxFileIndex) > 0) {
                    maxFileIndex = names[i]
                }
            }
            maxIndex = Integer.parseInt(
                maxFileIndex.substring(
                    maxFileIndex.length - 6,
                    maxFileIndex.length - 4
                )
            )
        }
        return maxIndex
    }

    fun write(content: String) {
        try {
            buffer.put(TIME_FORMAT.get()!!.format(System.currentTimeMillis()) + " /" + content)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private fun flushForever() {
        ioThread.execute {
            while (true) {
                if (buffer.size > BUFFER_SIZE * 0.75 || buffer.size > 0 && System.currentTimeMillis() - recent > 10 * 1000) {
                    flush()
                }
                try {
                    TimeUnit.SECONDS.sleep(2)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun flush() {
        try {
            val fw = BufferedWriter(
                OutputStreamWriter(
                    FileOutputStream(getFileName(), true),
                    StandardCharsets.UTF_8
                )
            )
            val size = buffer.size
            for (i in 0 until size) {
                fw.write(buffer.take())
            }
            fw.flush()
            fw.close()
        } catch (e: FileNotFoundException) {
            val file = File(getFileName())
            if (!file.exists()) {
                try {
                    createFile(getFileName())
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            recent = System.currentTimeMillis()
        }
    }

    private fun getFileName(): String {
        //在两种情况下变更文件，一是文件大小超过限制，二是时间已非当天
        if (System.currentTimeMillis() > tomorrow) {
            fileIndex.set(1)
            fileName = String.format(
                Locale.CHINA,
                "%s%s_%02d.log",
                FILE_PREFIX,
                DATE_FORMAT.format(tomorrow),
                fileIndex.get()
            )
            tomorrow = tomorrowMills
        } else {
            val file = File(fileName!!)
            if (file.exists() && file.length() > FILE_MAX_SIZE) {
                fileName = String.format(
                    Locale.CHINA,
                    "%s%s_%02d.log",
                    FILE_PREFIX,
                    DATE_FORMAT.format(System.currentTimeMillis()),
                    fileIndex.incrementAndGet()
                )
            }
        }
        return fileName as String
    }

    companion object {

        private val DATE_FORMAT = SimpleDateFormat("yyyyMMdd", Locale.CHINA)

        private val TIME_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA)
            }
        }

        private val BUFFER_SIZE = 100

        private val LOG_FOLDER = "/log/"

        private val FILE_PREFIX =
            Environment.getExternalStorageDirectory().toString() + LOG_FOLDER

        val instance: LogFileWriter
            get() = Holder.INSTANCE
    }
}
