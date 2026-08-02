package com.simovic.simovicweather.feature.weather.data.datasource.location

import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal fun interface LocationNameDataSource {
    suspend fun getName(coordinates: Coordinates): LocationName?
}

internal class AndroidLocationNameDataSource(
    private val geocoder: Geocoder,
    private val ioDispatcher: CoroutineDispatcher,
) : LocationNameDataSource {
    override suspend fun getName(coordinates: Coordinates): LocationName? {
        if (!Geocoder.isPresent()) return null

        val address =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(
                        coordinates.latitude,
                        coordinates.longitude,
                        MAX_RESULTS,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: List<Address>) {
                                if (continuation.isActive) continuation.resume(addresses.firstOrNull())
                            }

                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        },
                    )
                }
            } else {
                getAddressLegacy(coordinates)
            }
        return address?.toLocationName()
    }

    @Suppress("DEPRECATION")
    private suspend fun getAddressLegacy(coordinates: Coordinates): Address? =
        withContext(ioDispatcher) {
            geocoder
                .getFromLocation(
                    coordinates.latitude,
                    coordinates.longitude,
                    MAX_RESULTS,
                )?.firstOrNull()
        }

    private fun Address.toLocationName() =
        LocationName(
            city = locality ?: subAdminArea,
            region = adminArea,
            country = countryName,
        )

    private companion object {
        const val MAX_RESULTS = 1
    }
}

internal data class LocationName(
    val city: String?,
    val region: String?,
    val country: String?,
)
