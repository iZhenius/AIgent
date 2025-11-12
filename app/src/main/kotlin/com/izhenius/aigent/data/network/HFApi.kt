package com.izhenius.aigent.data.network

import com.izhenius.aigent.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class HFApi(
    private val httpClient: OkHttpClient,
) {

    fun buildRequest(bodyJson: String): Request {
        val apiToken = BuildConfig.HF_API_KEY
        return Request.Builder()
            .url("https://router.huggingface.co/v1/responses")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiToken")
            .post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    fun execute(request: Request): Response {
        return httpClient.newCall(request).execute()
    }
}

