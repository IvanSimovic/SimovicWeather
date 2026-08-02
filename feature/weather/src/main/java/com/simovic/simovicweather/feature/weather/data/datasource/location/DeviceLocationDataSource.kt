package com.simovic.simovicweather.feature.weather.data.datasource.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

internal fun interface DeviceLocationDataSource {
    suspend fun getCoordinates(): Coordinates?
}

internal class FusedDeviceLocationDataSource(
    private val client: FusedLocationProviderClient,
    private val context: Context,
) : DeviceLocationDataSource {
    override suspend fun getCoordinates(): Coordinates? {
        if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Location permission is not granted")
        }

        val location =
            client.lastLocation.await()
                ?: withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
                    val cancellation = CancellationTokenSource()
                    try {
                        client
                            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
                            .await()
                    } finally {
                        cancellation.cancel()
                    }
                }
        return location?.let { Coordinates(it.latitude, it.longitude) }
    }

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 10_000L
    }
}
