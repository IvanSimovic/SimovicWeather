package com.simovic.simovicweather.feature.weather.data.mapper

import com.simovic.simovicweather.feature.weather.data.datasource.api.model.LocationApiModel
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationMapperTest {
    private val mapper = LocationMapper()

    @Test
    fun `maps geocoding result`() {
        val result =
            mapper.toDomain(
                LocationApiModel(
                    id = 1,
                    name = "Sarajevo",
                    latitude = 43.85,
                    longitude = 18.41,
                    country = "Bosnia and Herzegovina",
                    countryCode = "BA",
                    admin1 = "Federation of Bosnia and Herzegovina",
                ),
            )

        assertEquals(
            WeatherLocation(
                id = 1,
                name = "Sarajevo",
                region = "Federation of Bosnia and Herzegovina",
                country = "Bosnia and Herzegovina",
                coordinates = Coordinates(43.85, 18.41),
            ),
            result,
        )
    }

    @Test
    fun `uses country code when country name is missing`() {
        val result =
            mapper.toDomain(
                LocationApiModel(
                    id = 1,
                    name = "Sarajevo",
                    latitude = 43.85,
                    longitude = 18.41,
                    countryCode = "BA",
                ),
            )

        assertEquals("BA", result.country)
    }
}
