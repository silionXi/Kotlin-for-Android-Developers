package com.antonioleiva.weatherapp.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import com.antonioleiva.weatherapp.R
import com.antonioleiva.weatherapp.domain.commands.RequestForecastCommand
import com.antonioleiva.weatherapp.domain.model.ForecastList
import com.antonioleiva.weatherapp.extensions.DelegatesExt
import com.antonioleiva.weatherapp.ui.adapters.ForecastListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Kotlin中的类遵循一个简单的结构。
 * 尽管与Java有一点细微的差别。
 * 你可以使 用try.kotlinlang.org
 * 在不需要一个真正的项目和不需要部署到机器的前提下来测试一 些简单的代码范例
 */
class MainActivity : BaseActivity(), ToolbarManager {

    private val zipCode: Long by DelegatesExt.preference(this, SettingsActivity.ZIP_CODE,
            SettingsActivity.DEFAULT_ZIP)//立即调用，没有delay?
    override val toolbar by lazy {
        android.util.Log.d("silion_log", "MainActivity toolbar lazy")
        find<Toolbar>(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        android.util.Log.d("silion_log", "MainActivity onCreate")
        initToolbar()

        forecastList.layoutManager = LinearLayoutManager(this)
        attachToScroll(forecastList)
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("silion_log", "MainActivity onResume")
        android.util.Log.d("silion_log", "MainActivity onResume zipCode + 1 = (${zipCode + 1})")
        loadForecast()
    }

    /**
     * 这里的 async() 只是一个普通的函数（不是挂起函数），
     * 但 block 参数是一个 带有 suspend 修饰的函数类型，
     * 所以当传递一个 lambda 给 async() 时，这会 是一个挂起 lambda ，
     * 这样我们就可以在这里调用一个挂起函数了
     *
     * await() 函数可以是一个挂起函数(因此在 await(){} 语句块内仍然可以调用)，
     * 该函数会挂起协程直至指定操作完成并返回结果
     */
    private fun loadForecast() = async(UI) {
        val result = bg { RequestForecastCommand(zipCode).execute() }
        // val result : Deferred<ForecastList> = bg { RequestForecastCommand(zipCode).execute() }
        updateUI(result.await())
    }

    private fun updateUI(weekForecast: ForecastList) {
        val adapter = ForecastListAdapter(weekForecast) {
            startActivity<DetailActivity>(DetailActivity.ID to it.id,
                    DetailActivity.CITY_NAME to weekForecast.city)
        }
        /*val adapter = ForecastListAdapter(weekForecast, {
            startActivity<DetailActivity>(DetailActivity.ID to it.id,
                    DetailActivity.CITY_NAME to weekForecast.city)
        })*/
        forecastList.adapter = adapter
        toolbarTitle = "${weekForecast.city} (${weekForecast.country})"
    }
}
