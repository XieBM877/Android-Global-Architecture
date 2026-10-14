package com.example.globalarch.core.network

sealed class NetworkException(
    message: String?,
    cause: Throwable?,
) : Exception(message, cause) {
    class Offline(
        cause: Throwable,
    ) : NetworkException("Network unavailable", cause)

    class Http(
        val code: Int,
        cause: Throwable,
    ) : NetworkException("HTTP $code", cause)

    class Parse(
        cause: Throwable,
    ) : NetworkException("Malformed response", cause)

    class Unknown(
        cause: Throwable,
    ) : NetworkException(cause.message, cause)
}
