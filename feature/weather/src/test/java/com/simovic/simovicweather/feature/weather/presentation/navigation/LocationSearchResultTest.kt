package com.simovic.simovicweather.feature.weather.presentation.navigation

import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.presentation.screen.weather.LocationUiModel
import com.simovic.simovicweather.feature.weather.presentation.screen.weather.toLocationSearchResult
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationSearchResultTest {
    @Test
    fun `location selection maps to navigation result`() {
        val location =
            WeatherLocation(
                id = 12,
                name = "Mostar",
                region = "Federation of Bosnia and Herzegovina",
                country = "Bosnia and Herzegovina",
                coordinates = Coordinates(latitude = 43.3438, longitude = 17.8078),
            )

        val result =
            LocationUiModel(
                label = "Mostar, Bosnia and Herzegovina",
                location = location,
            ).toLocationSearchResult()

        assertEquals(
            LocationSearchResult.SelectedLocation(
                id = location.id,
                name = location.name,
                region = location.region,
                country = location.country,
                latitude = location.coordinates.latitude,
                longitude = location.coordinates.longitude,
            ),
            result,
        )
    }

    @Test
    fun `selected location result maps to domain location`() {
        val result =
            LocationSearchResult.SelectedLocation(
                id = 12,
                name = "Mostar",
                region = "Federation of Bosnia and Herzegovina",
                country = "Bosnia and Herzegovina",
                latitude = 43.3438,
                longitude = 17.8078,
            )

        val location = result.toWeatherLocation()

        assertEquals(result.id, location.id)
        assertEquals(result.name, location.name)
        assertEquals(result.region, location.region)
        assertEquals(result.country, location.country)
        assertEquals(result.latitude, location.coordinates.latitude, 0.0)
        assertEquals(result.longitude, location.coordinates.longitude, 0.0)
    }
}
