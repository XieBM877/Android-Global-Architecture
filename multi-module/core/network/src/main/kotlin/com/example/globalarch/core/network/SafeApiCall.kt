package com.example.globalarch.core.network

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

internal suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: HttpException) {
    Result.failure(NetworkException.Http(failure.code(), failure))
} catch (failure: JsonDataException) {
    Result.failure(NetworkException.Parse(failure))
} catch (failure: JsonEncodingException) {
    Result.failure(NetworkException.Parse(failure))
} catch (failure: IOException) {
    Result.failure(NetworkException.Offline(failure))
} catch (failure: Exception) {
    Result.failure(NetworkException.Unknown(failure))
}
