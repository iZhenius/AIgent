package com.izhenius.aigent.presentation.viewmodel

import android.util.Log
import com.izhenius.aigent.domain.model.AiModelEntity
import com.izhenius.aigent.domain.model.AiTemperatureEntity
import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageDataEntity
import com.izhenius.aigent.domain.model.ChatMessageEntity
import com.izhenius.aigent.domain.model.ChatRoleEntity
import com.izhenius.aigent.domain.repository.HFRepository
import com.izhenius.aigent.domain.repository.OpenAiRepository
import com.izhenius.aigent.presentation.mvi.ChatUiAction
import com.izhenius.aigent.presentation.mvi.ChatUiState
import com.izhenius.aigent.presentation.mvi.MviViewModel
import com.izhenius.aigent.util.launch

class ChatViewModel(
    private val openAiRepository: OpenAiRepository,
    private val hfRepository: HFRepository,
) : MviViewModel<ChatUiState, ChatUiAction>() {

    override fun setInitialUiState(): ChatUiState {
        return ChatUiState(
            assistantType = AssistantType.BUDDY,
            messages = emptyMap(),
            currentMessages = emptyList(),
            isLoading = false,
            aiTemperature = AiTemperatureEntity.MEDIUM,
            aiModel = AiModelEntity.GPT_5_MINI,
            availableAiModels = listOf(
                AiModelEntity.GPT_5,
                AiModelEntity.GPT_5_MINI,
                AiModelEntity.GPT_5_NANO,
            ),
        )
    }

    override fun onUiAction(uiAction: ChatUiAction) {
        when (uiAction) {
            is ChatUiAction.OnSendMessage -> sendMessage(uiAction.text)
            is ChatUiAction.OnChangeAssistantType -> updateAssistantType(uiAction.assistantType)
            is ChatUiAction.OnChangeTemperatureLevel -> updateTemperatureLevel(uiAction.aiTemperature)
            is ChatUiAction.OnChangeModel -> updateModel(uiAction.aiModel)
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

    private fun updateTemperatureLevel(aiTemperature: AiTemperatureEntity) {
        updateUiState {
            copy(aiTemperature = aiTemperature)
        }
    }

    private fun updateModel(aiModel: AiModelEntity) {
        updateUiState {
            copy(aiModel = aiModel)
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

            val aiMessage = when (uiState.aiModel) {
                AiModelEntity.GPT_5,
                AiModelEntity.GPT_5_MINI,
                AiModelEntity.GPT_5_NANO,
                    -> {
                    openAiRepository.sendInput(
                        assistantType = currentAssistantType,
                        input = updatedMessages,
                        aiModel = uiState.aiModel,
                        aiTemperature = uiState.aiTemperature,
                    )
                }

                AiModelEntity.ZAI_ORG,
                AiModelEntity.DEEP_SEEK,
                AiModelEntity.KIMI_K2,
                    -> {
                    hfRepository.sendInput(
                        assistantType = currentAssistantType,
                        input = updatedMessages,
                        aiModel = uiState.aiModel,
                        aiTemperature = uiState.aiTemperature,
                    )
                }
            }
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