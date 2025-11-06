package com.izhenius.aigent.presentation.viewmodel

import android.util.Log
import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageDataEntity
import com.izhenius.aigent.domain.model.ChatMessageEntity
import com.izhenius.aigent.domain.model.ChatRoleEntity
import com.izhenius.aigent.domain.repository.OpenAiRepository
import com.izhenius.aigent.presentation.mvi.ChatUiAction
import com.izhenius.aigent.presentation.mvi.ChatUiState
import com.izhenius.aigent.presentation.mvi.MviViewModel
import com.izhenius.aigent.util.launch

class ChatViewModel(
    private val aiRepository: OpenAiRepository,
) : MviViewModel<ChatUiState, ChatUiAction>() {

    override fun setInitialUiState(): ChatUiState {
        return ChatUiState(
            assistantType = AssistantType.SPECIALIST,
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
            val updatedMessages = uiState.messages + ChatMessageEntity(
                id = System.nanoTime().toString(),
                role = ChatRoleEntity.User,
                data = ChatMessageDataEntity(
                    text = text,
                    aiModel = "",
                    tokens = "",
                ),
            )
            updateUiState { copy(messages = updatedMessages, isLoading = true) }

            val aiMessage = aiRepository.sendInput(
                assistantType = uiState.assistantType,
                input = updatedMessages,
            )
            updateUiState {
                copy(
                    messages = updatedMessages + aiMessage,
                    isLoading = false,
                )
            }
        }
    }
}