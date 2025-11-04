package com.izhenius.aigent.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.izhenius.aigent.presentation.model.ChatMessage
import com.izhenius.aigent.presentation.model.ChatRole
import com.izhenius.aigent.presentation.mvi.ChatUiAction
import com.izhenius.aigent.presentation.mvi.ChatUiState
import com.izhenius.aigent.presentation.mvi.MviViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel : MviViewModel<ChatUiState, ChatUiAction>() {

    override fun setInitialUiState(): ChatUiState {
        return ChatUiState(
            messages = emptyList(),
        )
    }

    override fun onUiAction(uiAction: ChatUiAction) {
        when (uiAction) {
            is ChatUiAction.OnSendMessage -> sendMessage(uiAction.text)
        }
    }

    private fun sendMessage(text: String) {
        updateMessages(ChatRole.User, text)
        viewModelScope.launch {
            delay(500)
            updateMessages(ChatRole.Server, "You said: $text")
        }
    }

    private fun updateMessages(role: ChatRole, text: String) {
        updateUiState {
            copy(
                messages = messages + ChatMessage(
                    id = System.nanoTime(),
                    role = role,
                    text = text,
                ),
            )
        }
    }
}