package com.simovic.simovicweather.feature.weather.data

import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.simovic.simovicweather.feature.base.data.retrofit.FORECAST_RETROFIT
import com.simovic.simovicweather.feature.base.data.retrofit.GEOCODING_RETROFIT
import com.simovic.simovicweather.feature.weather.data.datasource.api.service.GeocodingRetrofitService
import com.simovic.simovicweather.feature.weather.data.datasource.api.service.WeatherRetrofitService
import com.simovic.simovicweather.feature.weather.data.datasource.location.AndroidLocationNameDataSource
import com.simovic.simovicweather.feature.weather.data.datasource.location.DeviceLocationDataSource
import com.simovic.simovicweather.feature.weather.data.datasource.location.FusedDeviceLocationDataSource
import com.simovic.simovicweather.feature.weather.data.datasource.location.LocationNameDataSource
import com.simovic.simovicweather.feature.weather.data.mapper.LocationMapper
import com.simovic.simovicweather.feature.weather.data.mapper.WeatherMapper
import com.simovic.simovicweather.feature.weather.data.repository.DeviceLocationRepositoryImpl
import com.simovic.simovicweather.feature.weather.data.repository.LocationRepositoryImpl
import com.simovic.simovicweather.feature.weather.data.repository.WeatherRepositoryImpl
import com.simovic.simovicweather.feature.weather.domain.repository.DeviceLocationRepository
import com.simovic.simovicweather.feature.weather.domain.repository.LocationRepository
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.Locale

internal val dataModule =
    module {
        single { get<Retrofit>(named(FORECAST_RETROFIT)).create(WeatherRetrofitService::class.java) }
        single { get<Retrofit>(named(GEOCODING_RETROFIT)).create(GeocodingRetrofitService::class.java) }
        single { LocationServices.getFusedLocationProviderClient(androidContext()) }
        single { Geocoder(androidContext(), Locale.getDefault()) }
        singleOf(::FusedDeviceLocationDataSource) { bind<DeviceLocationDataSource>() }
        single<LocationNameDataSource> { AndroidLocationNameDataSource(get(), Dispatchers.IO) }
        singleOf(::WeatherMapper)
        singleOf(::LocationMapper)
        singleOf(::WeatherRepositoryImpl) { bind<WeatherRepository>() }
        singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }
        singleOf(::DeviceLocationRepositoryImpl) { bind<DeviceLocationRepository>() }
    }
