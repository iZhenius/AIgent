package com.izhenius.aigent.presentation.viewmodel

import android.util.Log
import com.izhenius.aigent.domain.model.AiModelEntity
import com.izhenius.aigent.domain.model.AiTemperatureEntity
import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageDataEntity
import com.izhenius.aigent.domain.model.ChatMessageEntity
import com.izhenius.aigent.domain.model.ChatRoleEntity
import com.izhenius.aigent.domain.model.TokenDataEntity
import com.izhenius.aigent.domain.repository.HFRepository
import com.izhenius.aigent.domain.repository.OpenAiRepository
import com.izhenius.aigent.presentation.mvi.ChatUiAction
import com.izhenius.aigent.presentation.mvi.ChatUiState
import com.izhenius.aigent.presentation.mvi.MviViewModel
import com.izhenius.aigent.util.calculateInputTokenCost
import com.izhenius.aigent.util.calculateOutputTokenCost
import com.izhenius.aigent.util.calculateTotalTokenCost
import com.izhenius.aigent.util.launch
import kotlinx.coroutines.Job

class ChatViewModel(
    private val openAiRepository: OpenAiRepository,
    private val hfRepository: HFRepository,
) : MviViewModel<ChatUiState, ChatUiAction>() {

    private var sendMessageJob: Job? = null

    override fun setInitialUiState(): ChatUiState {
        return ChatUiState(
            assistantType = AssistantType.BUDDY,
            messages = emptyMap(),
            currentMessages = emptyList(),
            isLoading = false,
            aiTemperature = AiTemperatureEntity.LOW,
            aiModel = AiModelEntity.GPT_5_NANO,
            availableAiModels = listOf(
                AiModelEntity.GPT_5_NANO,
                AiModelEntity.GPT_5_MINI,
                AiModelEntity.GPT_5,
            ),
            totalTokenData = TokenDataEntity(),
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

    private fun calculateTotalTokenData(messages: List<ChatMessageEntity>): TokenDataEntity {
        val totalTokenDataItems = messages.filter { message ->
            message.role == ChatRoleEntity.Assistant
        }.groupBy { message ->
            message.data.aiModel
        }.map { (aiModel, messages) ->
            TokenDataEntity(
                inputTokens = messages.sumOf { it.tokenData.inputTokens },
                outputTokens = messages.sumOf { it.tokenData.outputTokens },
                totalTokens = messages.sumOf { it.tokenData.totalTokens },
                inputCost = messages.sumOf { aiModel.calculateInputTokenCost(it.tokenData.inputTokens) },
                outputCost = messages.sumOf { aiModel.calculateOutputTokenCost(it.tokenData.outputTokens) },
                totalCost = messages.sumOf {
                    aiModel.calculateTotalTokenCost(
                        it.tokenData.inputTokens,
                        it.tokenData.outputTokens,
                    )
                },
            )
        }
        return TokenDataEntity(
            inputTokens = totalTokenDataItems.sumOf { it.inputTokens },
            outputTokens = totalTokenDataItems.sumOf { it.outputTokens },
            totalTokens = totalTokenDataItems.sumOf { it.totalTokens },
            inputCost = totalTokenDataItems.sumOf { it.inputCost },
            outputCost = totalTokenDataItems.sumOf { it.outputCost },
            totalCost = totalTokenDataItems.sumOf { it.totalCost },
        )
    }

    private fun updateAssistantType(assistantType: AssistantType) {
        updateUiState {
            val currentMessages = messages[assistantType].orEmpty()
            copy(
                assistantType = assistantType,
                currentMessages = currentMessages,
                totalTokenData = calculateTotalTokenData(currentMessages),
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
            val currentMessages = updatedMessages[assistantType].orEmpty()
            copy(
                messages = updatedMessages,
                currentMessages = currentMessages,
                isLoading = false,
                totalTokenData = calculateTotalTokenData(currentMessages),
            )
        }
    }

    private fun sendMessage(text: String) {
        sendMessageJob?.cancel()
        sendMessageJob = launch(
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
                    aiModel = uiState.aiModel,
                ),
                tokenData = TokenDataEntity(),
            )
            val updatedMessages = currentMessages + userMessage

            updateUiState {
                val updatedMessagesMap = messages + (currentAssistantType to updatedMessages)
                val currentMessages = updatedMessagesMap[currentAssistantType].orEmpty()
                copy(
                    messages = updatedMessagesMap,
                    currentMessages = currentMessages,
                    isLoading = true,
                    totalTokenData = calculateTotalTokenData(currentMessages),
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
                val finalMessages = finalMessagesMap[currentAssistantType].orEmpty()
                copy(
                    messages = finalMessagesMap,
                    currentMessages = finalMessages,
                    isLoading = false,
                    totalTokenData = calculateTotalTokenData(finalMessages),
                )
            }
        }
    }
}
