package com.izhenius.aigent.presentation.mvi

import com.izhenius.aigent.domain.model.ChatMessageEntity

data class ChatUiState(
    val messages: List<ChatMessageEntity>,
    val isLoading: Boolean = false,
)

sealed interface ChatUiAction {

    data class OnSendMessage(val text: String) : ChatUiAction
}
