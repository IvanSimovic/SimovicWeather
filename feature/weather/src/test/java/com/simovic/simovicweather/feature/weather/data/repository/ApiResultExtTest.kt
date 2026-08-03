package com.simovic.simovicweather.feature.weather.data.repository

import com.simovic.simovicweather.feature.base.data.retrofit.apiresult.ApiResult
import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class ApiResultExtTest {
    @Test
    fun `successful result is transformed`() {
        val result = toDomainResult(ApiResult.Success("forecast"))

        assertEquals(Result.Success("forecast"), result)
    }

    @Test
    fun `rate limit error returns rate limited failure`() {
        val result = toDomainResult(ApiResult.Error(429, "Try later"))

        assertEquals(Result.Failure(AppFailure.RateLimited("Try later")), result)
    }

    @Test
    fun `http error returns server failure`() {
        val result = toDomainResult(ApiResult.Error(500, "Server error"))

        assertEquals(Result.Failure(AppFailure.Server(500, "Server error")), result)
    }

    @Test
    fun `io exception returns connectivity failure`() {
        val exception = IOException("No connection")

        val result = toDomainResult(ApiResult.Exception(exception)) as Result.Failure

        assertSame(exception, (result.reason as AppFailure.Connectivity).cause)
    }

    @Test
    fun `unexpected exception returns unknown failure`() {
        val exception = IllegalStateException("Unexpected response")

        val result = toDomainResult(ApiResult.Exception(exception)) as Result.Failure

        assertSame(exception, (result.reason as AppFailure.Unknown).cause)
    }

    private fun toDomainResult(result: ApiResult<String>): Result<String> = result.toDomain { Result.Success(it) }
}
