package com.simovic.simovicweather.feature.weather.data

import com.simovic.simovicweather.feature.base.data.retrofit.FORECAST_RETROFIT
import com.simovic.simovicweather.feature.base.data.retrofit.GEOCODING_RETROFIT
import com.simovic.simovicweather.feature.weather.data.datasource.api.service.GeocodingRetrofitService
import com.simovic.simovicweather.feature.weather.data.datasource.api.service.WeatherRetrofitService
import com.simovic.simovicweather.feature.weather.data.mapper.LocationMapper
import com.simovic.simovicweather.feature.weather.data.mapper.WeatherMapper
import com.simovic.simovicweather.feature.weather.data.repository.LocationRepositoryImpl
import com.simovic.simovicweather.feature.weather.data.repository.WeatherRepositoryImpl
import com.simovic.simovicweather.feature.weather.domain.repository.LocationRepository
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

internal val dataModule =
    module {
        single { get<Retrofit>(named(FORECAST_RETROFIT)).create(WeatherRetrofitService::class.java) }
        single { get<Retrofit>(named(GEOCODING_RETROFIT)).create(GeocodingRetrofitService::class.java) }
        singleOf(::WeatherMapper)
        singleOf(::LocationMapper)
        singleOf(::WeatherRepositoryImpl) { bind<WeatherRepository>() }
        singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }
    }
