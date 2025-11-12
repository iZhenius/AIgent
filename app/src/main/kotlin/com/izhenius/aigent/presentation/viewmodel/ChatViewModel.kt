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

    private fun calculateTokenTotals(messages: List<ChatMessageEntity>): Triple<Int, Int, Int> {
        val assistantMessages = messages.filter { it.role == ChatRoleEntity.Assistant }
        val totalInput = assistantMessages.sumOf { it.tokenData.input }
        val totalOutput = assistantMessages.sumOf { it.tokenData.output }
        val total = assistantMessages.sumOf { it.tokenData.total }
        return Triple(totalInput, totalOutput, total)
    }

    private fun updateAssistantType(assistantType: AssistantType) {
        updateUiState {
            val currentMessages = messages[assistantType].orEmpty()
            val (totalInput, totalOutput, total) = calculateTokenTotals(currentMessages)
            copy(
                assistantType = assistantType,
                currentMessages = currentMessages,
                totalInputTokens = totalInput,
                totalOutputTokens = totalOutput,
                totalTokens = total,
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
            val (totalInput, totalOutput, total) = calculateTokenTotals(currentMessages)
            copy(
                messages = updatedMessages,
                currentMessages = currentMessages,
                isLoading = false,
                totalInputTokens = totalInput,
                totalOutputTokens = totalOutput,
                totalTokens = total,
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
                    aiModel = "",
                ),
                tokenData = TokenDataEntity(
                    input = 0,
                    output = 0,
                    total = 0,
                ),
            )
            val updatedMessages = currentMessages + userMessage

            updateUiState {
                val updatedMessagesMap = messages + (currentAssistantType to updatedMessages)
                val currentMessages = updatedMessagesMap[currentAssistantType].orEmpty()
                val (totalInput, totalOutput, total) = calculateTokenTotals(currentMessages)
                copy(
                    messages = updatedMessagesMap,
                    currentMessages = currentMessages,
                    isLoading = true,
                    totalInputTokens = totalInput,
                    totalOutputTokens = totalOutput,
                    totalTokens = total,
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
                val (totalInput, totalOutput, total) = calculateTokenTotals(finalMessages)
                copy(
                    messages = finalMessagesMap,
                    currentMessages = finalMessages,
                    isLoading = false,
                    totalInputTokens = totalInput,
                    totalOutputTokens = totalOutput,
                    totalTokens = total,
                )
            }
        }
    }
}