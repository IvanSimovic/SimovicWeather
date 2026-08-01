package com.simovic.simovicweather.feature.weather.presentation.navigation

import android.os.Parcelable
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class LocationSearchResult : Parcelable {
    data object CurrentLocation : LocationSearchResult()

    data class SelectedLocation(
        val id: Long?,
        val name: String,
        val region: String?,
        val country: String?,
        val latitude: Double,
        val longitude: Double,
    ) : LocationSearchResult()
}

internal fun LocationSearchResult.SelectedLocation.toWeatherLocation() =
    WeatherLocation(
        id = id,
        name = name,
        region = region,
        country = country,
        coordinates = Coordinates(latitude, longitude),
    )
