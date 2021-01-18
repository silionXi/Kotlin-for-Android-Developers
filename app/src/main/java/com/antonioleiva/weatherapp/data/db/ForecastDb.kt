package com.antonioleiva.weatherapp.data.db

import com.antonioleiva.weatherapp.domain.datasource.ForecastDataSource
import com.antonioleiva.weatherapp.domain.model.ForecastList
import com.antonioleiva.weatherapp.extensions.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import java.util.*

/**
 * SQLDatabase: https://blog.csdn.net/carson_ho/article/details/53241633/
 */
class ForecastDb(private val forecastDbHelper: ForecastDbHelper = ForecastDbHelper.instance,
                 private val dataMapper: DbDataMapper = DbDataMapper()) : ForecastDataSource {

    override fun requestForecastByZipCode(zipCode: Long, date: Long) = forecastDbHelper.use {

        val dailyRequest = "${DayForecastTable.CITY_ID} = ? AND ${DayForecastTable.DATE} >= ?"
        val dailyForecast = select(DayForecastTable.NAME)
                .whereSimple(dailyRequest, zipCode.toString(), date.toString())
                .parseList { DayForecast(HashMap(it)) }

        val city = select(CityForecastTable.NAME)
                .whereSimple("${CityForecastTable.ID} = ?", zipCode.toString())
                .parseOpt { CityForecast(HashMap(it), dailyForecast) }

        city?.let { dataMapper.convertToDomain(it) }
        /*
        // 也可以用with？可以，但没必要
        // https://blog.csdn.net/qq910689331/article/details/106327364
        with(city) {
            if (this != null)
            dataMapper.convertToDomain(this)
        }*/
    }

    override fun requestDayForecast(id: Long) = forecastDbHelper.use {
        val forecast = select(DayForecastTable.NAME).byId(id).
                parseOpt { DayForecast(HashMap(it)) }

        forecast?.let { dataMapper.convertDayToDomain(it) }
    }

    fun saveForecast(forecast: ForecastList) = forecastDbHelper.use {

        clear(CityForecastTable.NAME)
        clear(DayForecastTable.NAME)

        with(dataMapper.convertFromDomain(forecast)) {
            // Kotlin中数组转为可变长参数，前面加*
            insert(CityForecastTable.NAME, *map.toVarargArray())
            //insert(CityForecastTable.NAME, *(map.toVarargArray()))
            //val array : Array<out Pair<String, Any?>> = arrayOf(Pair("1",1), Pair("2",2))
            //insert(CityForecastTable.NAME, *array)
            dailyForecast.forEach { insert(DayForecastTable.NAME, *it.map.toVarargArray()) }
        }
    }
}
