package com.izhenius.aigent.presentation.mvi

import com.izhenius.aigent.presentation.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage>,
    val isLoading: Boolean = false,
)

sealed interface ChatUiAction {

    data class OnSendMessage(val text: String): ChatUiAction
}
