package com.antonioleiva.weatherapp.extensions

import java.text.DateFormat
import java.util.*

/**
 * 扩展函数
 */
fun Long.toDateString(dateFormat: Int = DateFormat.MEDIUM): String {
    val df = DateFormat.getDateInstance(dateFormat, Locale.getDefault())
    return df.format(this)
}
