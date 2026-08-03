package com.simovic.simovicweather

import com.simovic.simovicweather.feature.base.data.retrofit.FORECAST_RETROFIT
import com.simovic.simovicweather.feature.base.data.retrofit.GEOCODING_RETROFIT
import com.simovic.simovicweather.feature.base.data.retrofit.apiresult.ApiResultAdapterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val appModule =
    module {
        single {
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        }
        single {
            HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }
        }
        single {
            OkHttpClient.Builder().addInterceptor(get<HttpLoggingInterceptor>()).build()
        }
        single(named(FORECAST_RETROFIT)) {
            createRetrofit("https://api.open-meteo.com/", get(), get())
        }
        single(named(GEOCODING_RETROFIT)) {
            createRetrofit("https://geocoding-api.open-meteo.com/", get(), get())
        }
    }

private fun createRetrofit(
    baseUrl: String,
    client: OkHttpClient,
    json: Json,
): Retrofit =
    Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .addCallAdapterFactory(ApiResultAdapterFactory())
        .build()
