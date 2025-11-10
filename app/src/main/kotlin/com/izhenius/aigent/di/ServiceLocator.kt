package com.izhenius.aigent.di

import com.izhenius.aigent.data.network.OpenAiApi
import com.izhenius.aigent.data.repository.OpenAiRepositoryImpl
import com.izhenius.aigent.domain.repository.OpenAiRepository
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

object ServiceLocator {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val openAiApi: OpenAiApi by lazy {
        OpenAiApi(okHttpClient)
    }

    val openAiRepository: OpenAiRepository by lazy {
        OpenAiRepositoryImpl(openAiApi)
    }
}

