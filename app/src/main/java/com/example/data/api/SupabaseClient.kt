package com.example.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun createApi(baseUrl: String): SupabaseApi? {
        val formattedUrl = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
        return try {
            Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(SupabaseApi::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
