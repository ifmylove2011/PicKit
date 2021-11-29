package com.xter.pickit.kit

import android.os.Build

/**
 * @Author XTER
 * @Date 2021/11/29 10:11
 * @Description
 */
object PlatformUtil{
    /**
     * 判断是否是Android Q版本
     *
     * @return
     */
    fun isQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * 判断是否是Android R版本
     *
     * @return
     */
    fun isR(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }
}