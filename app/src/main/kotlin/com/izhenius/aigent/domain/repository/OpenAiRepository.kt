package com.izhenius.aigent.domain.repository

import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageEntity

interface OpenAiRepository {

    suspend fun sendInput(
        assistantType: AssistantType,
        input: List<ChatMessageEntity>,
    ): ChatMessageEntity
}