package com.simovic.simovicweather.feature.weather.data.repository

import com.simovic.simovicweather.feature.base.data.retrofit.apiresult.ApiResult
import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import java.io.IOException

internal inline fun <T, R> ApiResult<T>.toDomain(transform: (T) -> Result<R>): Result<R> =
    when (this) {
        is ApiResult.Success -> transform(data)
        is ApiResult.Error ->
            if (code == HTTP_TOO_MANY_REQUESTS) {
                Result.Failure(AppFailure.RateLimited(message))
            } else {
                Result.Failure(AppFailure.Server(code, message))
            }
        is ApiResult.Exception ->
            if (throwable is IOException) {
                Result.Failure(AppFailure.Connectivity(throwable))
            } else {
                Result.Failure(AppFailure.Unknown(throwable))
            }
    }

private const val HTTP_TOO_MANY_REQUESTS = 429
