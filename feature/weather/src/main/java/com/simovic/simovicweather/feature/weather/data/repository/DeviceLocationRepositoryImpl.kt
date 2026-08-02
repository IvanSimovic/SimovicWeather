package com.simovic.simovicweather.feature.weather.data.repository

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.data.datasource.location.DeviceLocationDataSource
import com.simovic.simovicweather.feature.weather.data.datasource.location.LocationNameDataSource
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.DeviceLocationRepository
import kotlinx.coroutines.CancellationException

internal class DeviceLocationRepositoryImpl(
    private val deviceLocationDataSource: DeviceLocationDataSource,
    private val locationNameDataSource: LocationNameDataSource,
) : DeviceLocationRepository {
    override suspend fun getCurrentLocation(): Result<WeatherLocation> =
        try {
            val coordinates =
                deviceLocationDataSource.getCoordinates()
                    ?: return Result.Failure(AppFailure.LocationUnavailable)
            val name =
                try {
                    locationNameDataSource.getName(coordinates)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    null
                }
            Result.Success(
                WeatherLocation(
                    name = name?.city.orEmpty(),
                    region = name?.region,
                    country = name?.country,
                    coordinates = coordinates,
                ),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: SecurityException) {
            Result.Failure(AppFailure.PermissionDenied)
        } catch (throwable: Exception) {
            Result.Failure(AppFailure.Unknown(throwable))
        }
}
