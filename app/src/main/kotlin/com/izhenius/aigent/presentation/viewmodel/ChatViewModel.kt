package com.izhenius.aigent.presentation.viewmodel

import android.util.Log
import com.izhenius.aigent.domain.repository.OpenAiRepository
import com.izhenius.aigent.presentation.model.ChatMessage
import com.izhenius.aigent.presentation.model.ChatRole
import com.izhenius.aigent.presentation.mvi.ChatUiAction
import com.izhenius.aigent.presentation.mvi.ChatUiState
import com.izhenius.aigent.presentation.mvi.MviViewModel
import com.izhenius.aigent.util.launch

class ChatViewModel(
    private val aiRepository: OpenAiRepository,
) : MviViewModel<ChatUiState, ChatUiAction>() {

    override fun setInitialUiState(): ChatUiState {
        return ChatUiState(
            messages = emptyList(),
            isLoading = false,
        )
    }

    override fun onUiAction(uiAction: ChatUiAction) {
        when (uiAction) {
            is ChatUiAction.OnSendMessage -> sendMessage(uiAction.text)
        }
    }

    private fun sendMessage(text: String) {
        launch(
            onError = {
                Log.e("OpenAiRepository", it.stackTraceToString())
                updateUiState { copy(isLoading = false) }
            },
        ) {
            updateMessages(ChatRole.User, text)
            updateUiState { copy(isLoading = true) }
            val aiText = aiRepository.sendInput(text)
            updateMessages(ChatRole.Server, aiText)
            updateUiState { copy(isLoading = false) }
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