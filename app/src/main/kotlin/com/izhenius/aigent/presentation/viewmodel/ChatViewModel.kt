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
            assistantType = AssistantType.BUDDY,
            messages = emptyMap(),
            currentMessages = emptyList(),
            isLoading = false,
        )
    }

    override fun onUiAction(uiAction: ChatUiAction) {
        when (uiAction) {
            is ChatUiAction.OnSendMessage -> sendMessage(uiAction.text)
            is ChatUiAction.OnChangeAssistantType -> updateAssistantType(uiAction.assistantType)
            is ChatUiAction.OnClearChat -> clearChat()
        }
    }

    private fun updateAssistantType(assistantType: AssistantType) {
        updateUiState {
            copy(
                assistantType = assistantType,
                currentMessages = messages[assistantType].orEmpty(),
            )
        }
    }

    private fun clearChat() {
        updateUiState {
            val updatedMessages = messages - assistantType
            copy(
                messages = updatedMessages,
                currentMessages = updatedMessages[assistantType].orEmpty(),
                isLoading = false,
            )
        }
    }

    private fun sendMessage(text: String) {
        launch(
            onError = {
                Log.e("OpenAiRepository", it.stackTraceToString())
                updateUiState { copy(isLoading = false) }
            },
        ) {
            val currentAssistantType = uiState.assistantType
            val currentMessages = uiState.messages[currentAssistantType].orEmpty()

            val userMessage = ChatMessageEntity(
                id = System.nanoTime().toString(),
                role = ChatRoleEntity.User,
                data = ChatMessageDataEntity(
                    text = text,
                    aiModel = "",
                    tokens = "",
                ),
            )
            val updatedMessages = currentMessages + userMessage

            updateUiState {
                val updatedMessagesMap = messages + (currentAssistantType to updatedMessages)
                copy(
                    messages = updatedMessagesMap,
                    currentMessages = updatedMessagesMap[currentAssistantType].orEmpty(),
                    isLoading = true,
                )
            }

            val aiMessage = aiRepository.sendInput(
                assistantType = currentAssistantType,
                input = updatedMessages,
            )
            val finalMessages = updatedMessages + aiMessage

            updateUiState {
                val finalMessagesMap = messages + (currentAssistantType to finalMessages)
                copy(
                    messages = finalMessagesMap,
                    currentMessages = finalMessagesMap[currentAssistantType].orEmpty(),
                    isLoading = false,
                )
            }
        }
    }
}