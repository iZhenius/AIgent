package com.izhenius.aigent.presentation.mvi

import com.izhenius.aigent.domain.model.AiModelEntity
import com.izhenius.aigent.domain.model.AiTemperatureEntity
import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageEntity

data class ChatUiState(
    val assistantType: AssistantType,
    val messages: Map<AssistantType, List<ChatMessageEntity>>,
    val currentMessages: List<ChatMessageEntity>,
    val isLoading: Boolean,
    val aiTemperature: AiTemperatureEntity,
    val aiModel: AiModelEntity,
    val availableAiModels: List<AiModelEntity>,
    val totalInputTokens: Int = 0,
    val totalOutputTokens: Int = 0,
    val totalTokens: Int = 0,
)

sealed interface ChatUiAction {

    data class OnSendMessage(val text: String) : ChatUiAction
    data class OnChangeAssistantType(val assistantType: AssistantType) : ChatUiAction
    data class OnChangeTemperatureLevel(val aiTemperature: AiTemperatureEntity) : ChatUiAction
    data class OnChangeModel(val aiModel: AiModelEntity) : ChatUiAction
    object OnClearChat : ChatUiAction
}
