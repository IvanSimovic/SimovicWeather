package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import com.simovic.simovicweather.feature.weather.R
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherSceneArtworkTest {
    @Test
    fun `every scene contains six unique frames`() {
        WeatherScene.entries.forEach { scene ->
            assertEquals(6, scene.frameResources.size)
            assertEquals(6, scene.frameResources.distinct().size)
        }
    }

    @Test
    fun `scene frames stay in authored order`() {
        val expectedFrames =
            mapOf(
                WeatherScene.SUNNY to
                    listOf(
                        R.drawable.weather_sunny_0,
                        R.drawable.weather_sunny_1,
                        R.drawable.weather_sunny_2,
                        R.drawable.weather_sunny_3,
                        R.drawable.weather_sunny_4,
                        R.drawable.weather_sunny_5,
                    ),
                WeatherScene.SUNNY_WINDY to
                    listOf(
                        R.drawable.weather_sunny_windy_0,
                        R.drawable.weather_sunny_windy_1,
                        R.drawable.weather_sunny_windy_2,
                        R.drawable.weather_sunny_windy_3,
                        R.drawable.weather_sunny_windy_4,
                        R.drawable.weather_sunny_windy_5,
                    ),
                WeatherScene.RAIN to
                    listOf(
                        R.drawable.weather_rain_0,
                        R.drawable.weather_rain_1,
                        R.drawable.weather_rain_2,
                        R.drawable.weather_rain_3,
                        R.drawable.weather_rain_4,
                        R.drawable.weather_rain_5,
                    ),
                WeatherScene.RAIN_WINDY to
                    listOf(
                        R.drawable.weather_rain_wind_0,
                        R.drawable.weather_rain_wind_1,
                        R.drawable.weather_rain_wind_2,
                        R.drawable.weather_rain_wind_3,
                        R.drawable.weather_rain_wind_4,
                        R.drawable.weather_rain_wind_5,
                    ),
            )

        expectedFrames.forEach { (scene, frames) -> assertEquals(frames, scene.frameResources) }
    }
}
