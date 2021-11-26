package com.xter.pickit.kit

import android.text.TextUtils
import android.util.Log

/**
 * 日志工具类
 */
object L {

    /**
     * 调试标志，为true则显示实时日志，为false则写入日志文件
     */
    var DEBUG: Boolean = true

    fun v(msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(4, 4)
            if (DEBUG) {
                Log.v(tag, msg)
            } else {
                //				writeToFile(tag + ":" + msg);
            }
        }
    }

    fun d(msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(4, 4)
            if (DEBUG) {
                Log.d(tag, msg)
            } else {
                writeToFile("$tag:$msg")
            }
        }
    }

    fun i(msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(4, 4)
            if (DEBUG) {
                Log.i(tag, msg)
            } else {
                writeToFile("$tag:$msg")
            }
        }
    }

    fun w(msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(4, 4)
            if (DEBUG) {
                Log.w(tag, msg)
            } else {
                writeToFile("$tag:$msg")
            }
        }
    }

    fun e(msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(4, 4)
            if (DEBUG) {
                Log.e(tag, msg)
            } else {
                writeToFile("$tag:$msg")
            }
        }
    }

    private fun writeToFile(content: String) {
        LogFileWriter.instance.write(content + "\r\n")
    }

    fun v(tag: String, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.v(tag, msg)
    }

    fun d(tag: String, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.i(tag, msg)
    }

    fun w(tag: String, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.w(tag, msg)
    }

    fun e(tag: String, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.e(tag, msg)
    }

    fun v(classPrior: Int, methodPriorint: Int, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.v(getMethodPath(classPrior, methodPriorint), msg)
    }

    fun d(classPrior: Int, methodPrior: Int, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.d(getMethodPath(classPrior, methodPrior), msg)
    }

    fun i(classPrior: Int, methodPrior: Int, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.i(getMethodPath(classPrior, methodPrior), msg)
    }

    fun w(classPrior: Int, methodPrior: Int, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.w(getMethodPath(classPrior, methodPrior), msg)
    }

    fun e(classPrior: Int, methodPrior: Int, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.e(getMethodPath(classPrior, methodPrior), msg)
    }

    fun v(prior: Int, msg: String) {
        if (DEBUG && !TextUtils.isEmpty(msg))
            Log.v(getMethodPath(prior, prior), msg)
    }

    fun d(prior: Int, msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(prior, prior)
            if (DEBUG) {
                Log.d(getMethodPath(prior, prior), msg)
            } else {
                writeToFile("$tag:$msg")
            }
        }
    }

    fun i(prior: Int, msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(prior, prior)
            if (DEBUG) {
                Log.i(getMethodPath(prior, prior), msg)
            } else {
                writeToFile("$tag:$msg")
            }
        }
    }

    fun w(prior: Int, msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(prior, prior)
            if (DEBUG) {
                Log.w(getMethodPath(prior, prior), msg)
            } else {
                writeToFile("$tag:$msg")
            }
        }
    }

    fun e(prior: Int, msg: String) {
        if (!TextUtils.isEmpty(msg)) {
            val tag = getMethodPath(prior, prior)
            if (DEBUG) {
                Log.e(getMethodPath(prior, prior), msg)
            } else {
                writeToFile("$tag:$msg")
            }
        }
    }

    fun exception(prior: Int, ex: Throwable) {
        w(prior, ex.toString())
        //		L.w(prior, track(ex));
    }

    private fun track(e: Throwable?): String? {
        val sb = StringBuffer()
        if (e != null) {
            for (element in e.stackTrace) {
                sb.append(element).append("\r\n\t")
            }
            if (sb.length > 1) {
                sb.deleteCharAt(sb.length - 1)
            }
        }
        return if (sb.length == 0) null else sb.toString()
    }

    /**
     * 得到调用此方法的类名与方法名
     *
     * @param classPrior  类级
     * @param methodPrior 方法级
     * @return string
     */
    fun getMethodPath(classPrior: Int, methodPrior: Int): String {
        val stackTrace = Thread.currentThread().stackTrace

        val targetElement = stackTrace[classPrior]
        val fileName = targetElement.fileName

        val methodName = targetElement.methodName
        var lineNumber = targetElement.lineNumber

        if (lineNumber < 0) {
            lineNumber = 0
        }
        val length = Thread.currentThread().stackTrace.size
        return if (classPrior > length || methodPrior > length) {
            ""
        } else
            "($fileName:$lineNumber)#$methodName-->"
    }

    /**
     * 得到调用此方法的类名与方法名（带包名）
     *
     * @param classPrior  类级
     * @param methodPrior 方法级
     * @return string
     */
    fun getPackageMethodPath(classPrior: Int, methodPrior: Int): String {
        val stackTrace = Thread.currentThread().stackTrace

        val targetElement = stackTrace[classPrior]
        val className = targetElement.className
        val methodName = targetElement.methodName
        var lineNumber = targetElement.lineNumber

        if (lineNumber < 0) {
            lineNumber = 0
        }
        val length = Thread.currentThread().stackTrace.size
        return if (classPrior > length || methodPrior > length) {
            ""
        } else
            "($className:$lineNumber)#$methodName-->"
    }

    /**
     * 测试方法，将线程中的序列全部输出
     */
    fun logThreadSequence() {
        val length = Thread.currentThread().stackTrace.size
        for (i in 0 until length) {
            Log.i(
                Thread.currentThread().stackTrace[i].className,
                Thread.currentThread().stackTrace[i].methodName
            )
        }
    }

}
