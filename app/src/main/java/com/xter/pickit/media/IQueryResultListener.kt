package com.xter.pickit.media

/**
 * @Author XTER
 * @Date 2021/11/29 9:49
 * @Description
 */
interface IQueryResultListener<T> {
    fun onCompleted(data:MutableList<T>){}
    fun onCompleted(data:T){}
}