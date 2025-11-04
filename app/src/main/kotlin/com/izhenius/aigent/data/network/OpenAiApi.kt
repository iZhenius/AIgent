package com.izhenius.aigent.data.network

import com.izhenius.aigent.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class OpenAiApi(
    private val httpClient: OkHttpClient,
) {

    fun buildRequest(bodyJson: String): Request {
        val apiKey = BuildConfig.OPENAI_API_KEY
        return Request.Builder()
            .url("https://api.openai.com/v1/responses")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    fun execute(request: Request): Response {
        return httpClient.newCall(request).execute()
    }
}
