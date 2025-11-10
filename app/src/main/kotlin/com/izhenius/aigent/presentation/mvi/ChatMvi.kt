package com.izhenius.aigent.presentation.mvi

import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageEntity

data class ChatUiState(
    val assistantType: AssistantType,
    val messages: Map<AssistantType, List<ChatMessageEntity>>,
    val currentMessages: List<ChatMessageEntity>,
    val isLoading: Boolean = false,
)

sealed interface ChatUiAction {

    data class OnSendMessage(val text: String) : ChatUiAction
    data class OnChangeAssistantType(val assistantType: AssistantType) : ChatUiAction
    object OnClearChat : ChatUiAction
}
