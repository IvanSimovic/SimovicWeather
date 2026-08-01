package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.annotation.DrawableRes
import com.simovic.simovicweather.feature.weather.R

internal const val WEATHER_FRAME_DURATION_MILLIS = 160L

internal val WeatherScene.frameResources: List<Int>
    @DrawableRes
    get() =
        when (this) {
            WeatherScene.SUNNY -> SunnyFrames
            WeatherScene.SUNNY_WINDY -> SunnyWindyFrames
            WeatherScene.RAIN -> RainFrames
            WeatherScene.RAIN_WINDY -> RainWindyFrames
        }

private val SunnyFrames =
    listOf(
        R.drawable.weather_sunny_0,
        R.drawable.weather_sunny_1,
        R.drawable.weather_sunny_2,
        R.drawable.weather_sunny_3,
        R.drawable.weather_sunny_4,
        R.drawable.weather_sunny_5,
    )

private val SunnyWindyFrames =
    listOf(
        R.drawable.weather_sunny_windy_0,
        R.drawable.weather_sunny_windy_1,
        R.drawable.weather_sunny_windy_2,
        R.drawable.weather_sunny_windy_3,
        R.drawable.weather_sunny_windy_4,
        R.drawable.weather_sunny_windy_5,
    )

private val RainFrames =
    listOf(
        R.drawable.weather_rain_0,
        R.drawable.weather_rain_1,
        R.drawable.weather_rain_2,
        R.drawable.weather_rain_3,
        R.drawable.weather_rain_4,
        R.drawable.weather_rain_5,
    )

private val RainWindyFrames =
    listOf(
        R.drawable.weather_rain_wind_0,
        R.drawable.weather_rain_wind_1,
        R.drawable.weather_rain_wind_2,
        R.drawable.weather_rain_wind_3,
        R.drawable.weather_rain_wind_4,
        R.drawable.weather_rain_wind_5,
    )
