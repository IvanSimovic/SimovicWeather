package com.simovic.simovicweather.feature.weather.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeocodingResponse(
    val results: List<LocationApiModel> = emptyList(),
)

@Serializable
internal data class LocationApiModel(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val admin1: String? = null,
)
