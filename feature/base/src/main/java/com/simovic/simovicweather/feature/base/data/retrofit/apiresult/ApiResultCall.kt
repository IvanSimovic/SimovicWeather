package com.simovic.simovicweather.feature.base.data.retrofit.apiresult

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class ApiResultCall<T>(
    private val delegate: Call<T>,
) : Call<ApiResult<T>> {
    override fun enqueue(callback: Callback<ApiResult<T>>) {
        delegate.enqueue(
            object : Callback<T> {
                override fun onResponse(
                    call: Call<T>,
                    response: Response<T>,
                ) {
                    val result =
                        response.body()?.takeIf { response.isSuccessful }?.let(ApiResult<T>::Success)
                            ?: ApiResult.Error(response.code(), response.message())
                    callback.onResponse(this@ApiResultCall, Response.success(result))
                }

                override fun onFailure(
                    call: Call<T>,
                    throwable: Throwable,
                ) {
                    callback.onResponse(this@ApiResultCall, Response.success(ApiResult.Exception(throwable)))
                }
            },
        )
    }

    override fun clone(): Call<ApiResult<T>> = ApiResultCall(delegate.clone())

    override fun execute(): Response<ApiResult<T>> = throw UnsupportedOperationException("Synchronous execution is not supported")

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()
}
