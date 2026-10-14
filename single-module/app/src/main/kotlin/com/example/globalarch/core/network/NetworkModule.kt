package com.example.globalarch.core.network

import com.example.globalarch.BuildConfig
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val IO_TIMEOUT_SECONDS = 30L

internal fun createMoshi(): Moshi = Moshi.Builder().build()

internal fun createOkHttpClient(enableLogging: Boolean): OkHttpClient = OkHttpClient
    .Builder()
    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .readTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .writeTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .apply {
        if (enableLogging) {
            addInterceptor(
                HttpLoggingInterceptor { message -> Timber.tag("OkHttp").d(message) }.apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("Authorization")
                },
            )
        }
    }.build()

internal fun createRetrofit(
    baseUrl: String,
    client: OkHttpClient,
    moshi: Moshi,
): Retrofit = Retrofit
    .Builder()
    .baseUrl(baseUrl)
    .client(client)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun provideNotesRemoteDataSource(): NotesRemoteDataSource {
        val baseUrl = BuildConfig.API_BASE_URL
        if (baseUrl.isBlank()) return NoOpNotesRemoteDataSource
        val retrofit = createRetrofit(baseUrl, createOkHttpClient(BuildConfig.DEBUG), createMoshi())
        return RetrofitNotesRemoteDataSource(retrofit.create(NotesApi::class.java))
    }
}
