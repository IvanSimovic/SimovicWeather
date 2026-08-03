package com.simovic.simovicweather.feature.base.data.retrofit.apiresult

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiResultAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        require(returnType is ParameterizedType) { "Call return type must be parameterized" }

        val apiResultType = getParameterUpperBound(0, returnType)
        if (getRawType(apiResultType) != ApiResult::class.java) return null
        require(apiResultType is ParameterizedType) { "ApiResult must be parameterized" }

        return ApiResultCallAdapter<Any>(getParameterUpperBound(0, apiResultType))
    }
}
