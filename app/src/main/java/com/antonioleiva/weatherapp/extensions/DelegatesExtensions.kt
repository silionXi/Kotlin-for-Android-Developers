package com.antonioleiva.weatherapp.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KProperty

object DelegatesExt {
    fun <T> notNullSingleValue() = NotNullSingleValueVar<T>()
    fun <T> preference(context: Context, name: String,
            default: T) = Preference(context, name, default).also { p -> print("DelegatesExt 代理 $p") }
    /*fun <T> preference(context: Context, name: String,
                       default: T) : Preference<T> {
        android.util.Log.d("silion_log", "DelegatesExt preference 调用")
        return Preference(context, name, default).also { p -> print("DelegatesExt 代理 $p") }
    }*/
}

class NotNullSingleValueVar<T> {

    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
            value ?: throw IllegalStateException("${property.name} not initialized")

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = if (this.value == null) value
        else throw IllegalStateException("${property.name} already initialized")
    }
}

class Preference<T>(private val context: Context, private val name: String,
        private val default: T) {

    /**
     * 主构造函数不能包含任何的代码。
     * 初始化的代码可以放到以 init 关键字作为前缀的初始化块（initializer blocks）中
     */
    init {
        android.util.Log.d("silion_log", "Preference init 初始化块")
    }

    /**
     * 属性代理
     */
    private val prefs: SharedPreferences by lazy {
        android.util.Log.d("silion_log", "Preference prefs lazy 调用")
        context.getSharedPreferences("default", Context.MODE_PRIVATE)
    }

     operator fun getValue(thisRef: Any?, property: KProperty<*>): T = findPreference(name, default).also(::println)
    /*operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        android.util.Log.d("silion_log", "Preference getValue 调用")
        return findPreference(name, default).also(::println)
    }*/

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(name, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun findPreference(name: String, default: T): T = with(prefs) {
        android.util.Log.d("silion_log", "Preference findPreference 调用")
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("This type can be saved into Preferences")
        }

        res as T
    }

    @SuppressLint("CommitPrefEdits")
    private fun putPreference(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }.apply()
    }
}