package com.simovic.simovicweather.feature.weather.data.mapper

import com.simovic.simovicweather.feature.weather.data.datasource.api.model.LocationApiModel
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation

internal class LocationMapper {
    fun toDomain(model: LocationApiModel): WeatherLocation =
        WeatherLocation(
            id = model.id,
            name = model.name,
            region = model.admin1,
            country = model.country ?: model.countryCode,
            coordinates = Coordinates(model.latitude, model.longitude),
        )
}
