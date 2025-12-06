package com.example.ptvtimetable.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PTVApiClient {
    private const val BASE_URL = "https://timetableapi.ptv.vic.gov.au/"

    // PTV API Credentials - loaded from local.properties via BuildConfig
    private val DEV_ID = com.example.ptvtimetable.BuildConfig.PTV_DEV_ID
    private val API_KEY = com.example.ptvtimetable.BuildConfig.PTV_API_KEY

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Generates HMAC-SHA1 signature for PTV API authentication
     */
    private fun generateSignature(request: String): String {
        val keySpec = SecretKeySpec(API_KEY.toByteArray(Charsets.UTF_8), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(keySpec)
        val result = mac.doFinal(request.toByteArray(Charsets.UTF_8))
        return result.joinToString("") { "%02x".format(it) }.uppercase()
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Build the request path with query parameters including devid
        val requestPath = originalUrl.encodedPath +
                (if (originalUrl.encodedQuery != null) "?${originalUrl.encodedQuery}&" else "?") +
                "devid=$DEV_ID"

        // Generate signature
        val signature = generateSignature(requestPath)

        // Add devid and signature to URL
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("devid", DEV_ID)
            .addQueryParameter("signature", signature)
            .build()

        // Build new request with updated URL and headers
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build()

        chain.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: PTVApiService = retrofit.create(PTVApiService::class.java)

    // Keep for backward compatibility but no longer used
    @Deprecated("Token-based authentication is deprecated, using signature-based auth instead")
    fun setToken(newToken: String) {
        // No-op: signature-based authentication doesn't use tokens
    }
}
