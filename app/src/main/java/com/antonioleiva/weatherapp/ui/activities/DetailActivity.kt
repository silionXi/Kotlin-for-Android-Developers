package com.antonioleiva.weatherapp.ui.activities

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.antonioleiva.weatherapp.R
import com.antonioleiva.weatherapp.domain.commands.RequestDayForecastCommand
import com.antonioleiva.weatherapp.domain.model.Forecast
import com.antonioleiva.weatherapp.extensions.color
import com.antonioleiva.weatherapp.extensions.textColor
import com.antonioleiva.weatherapp.extensions.toDateString
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.Ref
import org.jetbrains.anko.coroutines.experimental.asReference
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import java.text.DateFormat

class DetailActivity : AppCompatActivity(), ToolbarManager {

    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    /**
     * 类内部的对象声明可以用 companion 关键字标记
     * 每个类只能有一个伴生对象
     *
     * 即使伴生对象的成员看起来像其他语言的静态成员，
     * 在运行时他们仍然是真实对象的实例成员，而且还可以实现接口
     *
     * 在 JVM 平台，如果使用 @JvmStatic 注解，你可以将伴生对象的成员生成为真正的静态方法和字段
     * @link https://blog.csdn.net/maosidiaoxian/article/details/81778057
     */
    companion object {
        val ID = "DetailActivity:id"
        val CITY_NAME = "DetailActivity:cityName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        initToolbar()

        toolbarTitle = intent.getStringExtra(CITY_NAME)
        enableHomeAsUp { onBackPressed() }

        async(UI) {
            val result = bg { RequestDayForecastCommand(intent.getLongExtra(ID, -1)).execute() }
            bindForecast(result.await())
        }

        /*async(UI, block = {
            val result = bg { RequestDayForecastCommand(intent.getLongExtra(ID, -1)).execute() }
            bindForecast(result.await())
        })*/
    }

    private fun bindForecast(forecast: Forecast) = with(forecast) {
        Picasso.with(ctx).load(iconUrl).into(icon)
        toolbar.subtitle = date.toDateString(DateFormat.FULL)
        weatherDescription.text = description
        bindWeather(high to maxTemperature, low to minTemperature)
    }

    private fun bindWeather(vararg views: Pair<Int, TextView>) = views.forEach {
        it.second.text = "${it.first}º"
        it.second.textColor = color(when (it.first) {
            in -50..0 -> android.R.color.holo_red_dark
            in 0..15 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_green_dark
        })
    }
}