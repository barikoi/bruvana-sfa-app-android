package com.barikoi.cnlapp.base.api

sealed class ApiState<T>(
    val data: T? = null,
    val error: Failure? = null
) {
    class Success<T>(data: T) : ApiState<T>(data)
    class Loading<T>(data: T? = null) : ApiState<T>(data)
    class Empty<T>(data: T? = null) : ApiState<T>(data)
    class Error<T>(throwable: Failure, data: T? = null) : ApiState<T>(data, throwable)
}