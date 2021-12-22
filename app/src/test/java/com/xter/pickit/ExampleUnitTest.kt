package com.xter.pickit

import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun calc() {
        val str = "PhotoView(20)"
        println(str.substring(0, str.indexOf("(")))
    }

    @Test
    fun testMap() {
        val list = mutableListOf<String>("aa1aa", "bb2bb", "cc3cc")
        list.flatMap {
            listOf(it, it + 1)
        }.let {
            println(it)
        }

        list.map {
            it.subSequence(2,3)
        }.let {
            println("-----")
            println(it)
            recVargs(*(it.toTypedArray()))
        }
    }

    fun recVargs(vararg data:CharSequence){
        println(data.contentToString())
    }

    @Test
    fun test111(){
    }
}