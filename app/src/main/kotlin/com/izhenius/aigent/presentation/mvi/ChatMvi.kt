package com.izhenius.aigent.presentation.mvi

import com.izhenius.aigent.domain.model.AiModelEntity
import com.izhenius.aigent.domain.model.AiTemperatureEntity
import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageEntity
import com.izhenius.aigent.domain.model.ChatRoleEntity
import com.izhenius.aigent.domain.model.TokenDataEntity
import com.izhenius.aigent.util.calculateInputTokenCost
import com.izhenius.aigent.util.calculateOutputTokenCost
import com.izhenius.aigent.util.calculateTotalTokenCost

data class ChatUiState(
    val assistantType: AssistantType,
    val messages: Map<AssistantType, List<ChatMessageEntity>>,
    val currentMessages: List<ChatMessageEntity>,
    val isLoading: Boolean,
    val aiTemperature: AiTemperatureEntity,
    val aiModel: AiModelEntity,
    val availableAiModels: List<AiModelEntity>,
    val totalTokenData: TokenDataEntity,
    val isSummarizationNeeded: Boolean,
    val summarizationThreshold: Int,
)

sealed interface ChatUiAction {

    data class OnSendMessage(val text: String) : ChatUiAction
    data class OnChangeAssistantType(val assistantType: AssistantType) : ChatUiAction
    data class OnChangeTemperatureLevel(val aiTemperature: AiTemperatureEntity) : ChatUiAction
    data class OnChangeModel(val aiModel: AiModelEntity) : ChatUiAction
    object OnClearChat : ChatUiAction
    data class OnIsSummarizationNeededCheck(val isChecked: Boolean) : ChatUiAction
}

object ChatUiReducer {

    fun initial(
        assistantType: AssistantType,
        aiTemperature: AiTemperatureEntity,
        aiModel: AiModelEntity,
        availableAiModels: List<AiModelEntity>,
    ): ChatUiState {
        return ChatUiState(
            assistantType = assistantType,
            messages = emptyMap(),
            currentMessages = emptyList(),
            isLoading = false,
            aiTemperature = aiTemperature,
            aiModel = aiModel,
            availableAiModels = availableAiModels,
            totalTokenData = TokenDataEntity(),
            isSummarizationNeeded = false,
            summarizationThreshold = 3,
        )
    }

    fun ChatUiState.assistantTypeChange(
        assistantType: AssistantType,
    ): ChatUiState {
        val currentMessages = messages[assistantType].orEmpty()
        return copy(
            assistantType = assistantType,
            currentMessages = currentMessages,
            totalTokenData = calculateTotalTokenData(currentMessages),
        )
    }

    fun ChatUiState.messagesChange(
        messages: Map<AssistantType, List<ChatMessageEntity>>,
        isLoading: Boolean,
    ): ChatUiState {
        val currentMessages = messages[assistantType].orEmpty()
        return copy(
            messages = messages,
            currentMessages = currentMessages,
            isLoading = isLoading,
            totalTokenData = calculateTotalTokenData(currentMessages),
        )
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
}
