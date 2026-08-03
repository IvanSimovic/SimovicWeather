package com.simovic.simovicweather.feature.base.data.retrofit.apiresult

import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ApiResultCallTest {
    @Test
    fun `successful response returns success`() {
        val result = execute(FakeCall(Response.success("forecast")))

        assertEquals(ApiResult.Success("forecast"), result)
    }

    @Test
    fun `http error returns status and message`() {
        val response = Response.error<String>(404, "Not found".toResponseBody())

        val result = execute(FakeCall(response)) as ApiResult.Error

        assertEquals(response.code(), result.code)
        assertEquals(response.message(), result.message)
    }

    @Test
    fun `transport failure returns exception`() {
        val exception = IOException("No connection")

        val result = execute<String>(FakeCall(failure = exception)) as ApiResult.Exception

        assertSame(exception, result.throwable)
    }

    private fun <T> execute(call: Call<T>): ApiResult<T> {
        var result: ApiResult<T>? = null
        ApiResultCall(call).enqueue(
            object : Callback<ApiResult<T>> {
                override fun onResponse(
                    call: Call<ApiResult<T>>,
                    response: Response<ApiResult<T>>,
                ) {
                    result = response.body()
                }

                override fun onFailure(
                    call: Call<ApiResult<T>>,
                    throwable: Throwable,
                ) = throw AssertionError("ApiResultCall must deliver failures as values", throwable)
            },
        )
        return requireNotNull(result)
    }

    private class FakeCall<T>(
        private val response: Response<T>? = null,
        private val failure: Throwable? = null,
    ) : Call<T> {
        override fun enqueue(callback: Callback<T>) {
            response?.let { callback.onResponse(this, it) }
                ?: callback.onFailure(this, requireNotNull(failure))
        }

        override fun clone(): Call<T> = FakeCall(response, failure)

        override fun execute(): Response<T> = throw UnsupportedOperationException()

        override fun isExecuted(): Boolean = false

        override fun cancel() = Unit

        override fun isCanceled(): Boolean = false

        override fun request(): Request = Request.Builder().url("https://example.com/").build()

        override fun timeout(): Timeout = Timeout.NONE
    }
}
