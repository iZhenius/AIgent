package com.izhenius.aigent.domain.repository

interface OpenAiRepository {

    suspend fun sendInput(input: String): String
}