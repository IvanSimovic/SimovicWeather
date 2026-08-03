package com.simovic.simovicweather.feature.weather.data

import com.simovic.simovicweather.feature.base.data.retrofit.FORECAST_RETROFIT
import com.simovic.simovicweather.feature.weather.data.datasource.api.service.WeatherRetrofitService
import com.simovic.simovicweather.feature.weather.data.mapper.WeatherMapper
import com.simovic.simovicweather.feature.weather.data.repository.WeatherRepositoryImpl
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

internal val dataModule =
    module {
        single { get<Retrofit>(named(FORECAST_RETROFIT)).create(WeatherRetrofitService::class.java) }
        singleOf(::WeatherMapper)
        singleOf(::WeatherRepositoryImpl) { bind<WeatherRepository>() }
    }
