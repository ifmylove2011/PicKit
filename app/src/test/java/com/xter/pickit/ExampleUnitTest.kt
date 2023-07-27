package com.xter.pickit

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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
    fun testChannel(){
        val channel = Channel<Int>()
        runBlocking {
            launch {
// 这⾥可能是消耗⼤量 CPU 运算的异步逻辑，我们将仅仅做 5 次整数的平⽅并发送
                for (x in 1..5) channel.send(x * x)
            }
// 这⾥我们打印了 5 次被接收的整数：
            repeat(5) { println(channel.receive()) }
            println("Done!")
        }
    }

    @Test
    fun testFlow1() = runBlocking<Unit> {
        foo().forEach { value ->
            println(System.currentTimeMillis())
            println(value)
        }

        foo1().collect { value ->
            println(System.currentTimeMillis())
            println(value) }
    }

    suspend fun foo(): List<Int> {
        delay(1000) // 假装我们在这⾥做了⼀些异步的事情
        return listOf(1, 2, 3)
    }

    fun foo1(): Flow<Int> = flow { // 流构建器
        for (i in 1..3) {
            delay(100) // 假装我们在这⾥做了⼀些有⽤的事情
            emit(i) // 发送下⼀个值
        }
    }

    @Test
    fun testCor1() = runBlocking {
        println(Thread.currentThread().name + "  R0")
        var value = 0
        launch(CoroutineName("R")) {
            println(Thread.currentThread().name + "  L0")
            delay(5000L)
            println(Thread.currentThread().name + "  L1")
        }
        println(Thread.currentThread().name + "  R1")
        coroutineScope {
            println(Thread.currentThread().name + "  CS0 START")
            launch(CoroutineName("CS")) {
                println(Thread.currentThread().name + "  LL0")
                delay(500L)
                println(Thread.currentThread().name + "  LL1")
            }
            coroutineScope {
                println(Thread.currentThread().name + "  CS1:START")
                launch(CoroutineName("CS1")) {
                    println(Thread.currentThread().name + "  CS1:L1")
                    delay(200L)
                    value = getValue()
                    println(Thread.currentThread().name + "  CS1:L2")
                }
                println(Thread.currentThread().name + "  CS1:END")
            }
            println(Thread.currentThread().name + "  CS0 MID")
            coroutineScope {
                println(Thread.currentThread().name + "  CS2")
                launch(CoroutineName("CS2")) {
                    println(Thread.currentThread().name + "  CS2:L1")
                    delay(200L)
                    println(Thread.currentThread().name + "  CS2:L2")
                }
                println(Thread.currentThread().name + "  CS2:END")
            }
            println(Thread.currentThread().name + "  CS0 END")
        }
        println("value=$value")
        println(Thread.currentThread().name + "  R2")
    }

    fun getValue(): Int = runBlocking {
        delay(2000L)
        200
    }

    @Test
    fun testCor() = runBlocking { // this: CoroutineScope
        println("O")
        launch {
            println("A")
            delay(200L)
            println("Task from runBlocking")
        }
        coroutineScope { // 创建⼀个协程作⽤域
            println("B")
            launch {
                println("D")
                delay(2500L)
                println("Task from nested launch")
            }
            println("C")
            coroutineScope {
                println("V")
                launch {
                    println("Z")
                    delay(500L)
                    println("Task from nested nested launch")
                }
            }
            delay(100L)
            println("Task from coroutine scope") // 这⼀⾏会在内嵌 launch 之前输出
        }
        coroutineScope {
            println("Q")
            launch {
                println("W")
                delay(500L)
                println("Task from another coroutine scope")
            }
        }
        println("E")
        println("Coroutine scope is over") // 这⼀⾏在内嵌 launch 执⾏完毕后才输出
    }

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
            it.subSequence(2, 3)
        }.let {
            println("-----")
            println(it)
            recVargs(*(it.toTypedArray()))
        }
    }

    fun recVargs(vararg data: CharSequence) {
        println(data.contentToString())
    }

    @Test
    fun test111() {
        for (i in 4 until 6) {
            print(i)
        }
        for (i in 4 - 1 downTo 2) {
            print(i)
        }
    }
}