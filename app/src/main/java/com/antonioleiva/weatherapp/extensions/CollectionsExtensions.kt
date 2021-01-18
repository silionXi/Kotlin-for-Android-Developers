package com.antonioleiva.weatherapp.extensions

import java.util.*

fun <K, V : Any> Map<K, V?>.toVarargArray(): Array<out Pair<K, V>> =
        map({ Pair(it.key, it.value!!) }).toTypedArray()

/**
 * 返回第一个结果，如果都为null，抛出异常
 * 也是扩展函数？
 * 在Kotlin中使用 inline 关键字来修饰函数，这些函数就成了内联函数。
 * 它们的函数体在编译的时期被嵌入到每一个调用的地方，
 * 以减少额外生成的匿名类数，以及函数执行的时间开销
 */
inline fun <T, R : Any> Iterable<T>.firstResult(predicate: (T) -> R?): R {
    for (element in this) {
        val result = predicate(element)
        if (result != null) return result
    }
    throw NoSuchElementException("No element matching predicate was found.")
}