package com.izhenius.aigent.domain.repository

import com.izhenius.aigent.domain.model.ChatMessageEntity

interface OpenAiRepository {

    suspend fun sendInput(input: List<ChatMessageEntity>): ChatMessageEntity
}