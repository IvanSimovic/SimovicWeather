package com.simovic.simovicweather.feature.base.domain.result

sealed interface Result<out T> {
    data class Success<T>(
        val value: T,
    ) : Result<T>

    data class Failure(
        val reason: AppFailure,
    ) : Result<Nothing>
}
