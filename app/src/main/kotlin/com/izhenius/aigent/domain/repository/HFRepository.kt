package com.izhenius.aigent.domain.repository

import com.izhenius.aigent.domain.model.AiModelEntity
import com.izhenius.aigent.domain.model.AiTemperatureEntity
import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageEntity

interface HFRepository {

    suspend fun sendInput(
        assistantType: AssistantType,
        input: List<ChatMessageEntity>,
        aiModel: AiModelEntity,
        aiTemperature: AiTemperatureEntity,
        isSummarization: Boolean,
    ): ChatMessageEntity
}
