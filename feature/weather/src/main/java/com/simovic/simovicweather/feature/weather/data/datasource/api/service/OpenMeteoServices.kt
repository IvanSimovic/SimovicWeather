package com.simovic.simovicweather.feature.weather.data.datasource.api.service

import com.simovic.simovicweather.feature.base.data.retrofit.apiresult.ApiResult
import com.simovic.simovicweather.feature.weather.data.datasource.api.model.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

private const val CURRENT_FIELDS =
    "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation," +
        "weather_code,pressure_msl,wind_speed_10m"
private const val DAILY_FIELDS =
    "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
private val FORECAST_OPTIONS =
    mapOf(
        "current" to CURRENT_FIELDS,
        "daily" to DAILY_FIELDS,
        "timezone" to "auto",
        "forecast_days" to "7",
    )

internal interface WeatherRetrofitService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @retrofit2.http.QueryMap options: Map<String, String> = FORECAST_OPTIONS,
    ): ApiResult<ForecastResponse>
}
