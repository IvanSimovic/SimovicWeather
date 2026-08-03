package com.simovic.simovicweather.feature.base.domain.result

sealed interface AppFailure {
    data class Connectivity(
        val cause: Throwable? = null,
    ) : AppFailure

    data class Server(
        val code: Int,
        val message: String? = null,
    ) : AppFailure

    data class RateLimited(
        val message: String? = null,
    ) : AppFailure

    data class MalformedData(
        val cause: Throwable? = null,
    ) : AppFailure

    data object PermissionDenied : AppFailure

    data object LocationUnavailable : AppFailure

    data class Unknown(
        val cause: Throwable? = null,
    ) : AppFailure
}
