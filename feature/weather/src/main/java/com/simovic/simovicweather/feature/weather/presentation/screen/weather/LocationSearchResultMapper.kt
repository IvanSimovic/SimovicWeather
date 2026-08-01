package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import com.simovic.simovicweather.feature.weather.presentation.navigation.LocationSearchResult

internal fun LocationUiModel.toLocationSearchResult(): LocationSearchResult.SelectedLocation =
    LocationSearchResult.SelectedLocation(
        id = location.id,
        name = location.name,
        region = location.region,
        country = location.country,
        latitude = location.coordinates.latitude,
        longitude = location.coordinates.longitude,
    )
