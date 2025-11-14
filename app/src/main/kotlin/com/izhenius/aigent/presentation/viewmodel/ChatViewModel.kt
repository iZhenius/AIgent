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
import com.izhenius.aigent.presentation.mvi.ChatUiReducer
import com.izhenius.aigent.presentation.mvi.ChatUiReducer.assistantTypeChange
import com.izhenius.aigent.presentation.mvi.ChatUiReducer.messagesChange
import com.izhenius.aigent.presentation.mvi.ChatUiState
import com.izhenius.aigent.presentation.mvi.MviViewModel
import com.izhenius.aigent.util.launch
import kotlinx.coroutines.Job

class ChatViewModel(
    private val openAiRepository: OpenAiRepository,
    private val hfRepository: HFRepository,
) : MviViewModel<ChatUiState, ChatUiAction>() {

    private var sendMessageJob: Job? = null
    private var summariseMessagesJob: Job? = null

    override fun setInitialUiState(): ChatUiState {
        return ChatUiReducer.initial(
            assistantType = AssistantType.BUDDY,
            aiTemperature = AiTemperatureEntity.LOW,
            aiModel = AiModelEntity.GPT_5_NANO,
            availableAiModels = listOf(
                AiModelEntity.GPT_5_NANO,
                AiModelEntity.GPT_5_MINI,
                AiModelEntity.GPT_5,
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
            is ChatUiAction.OnIsSummarizationNeededCheck -> handleOnIsSummarizationNeededCheck(uiAction.isChecked)
        }
    }

    private fun updateAssistantType(assistantType: AssistantType) {
        updateUiState {
            assistantTypeChange(assistantType)
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
            messagesChange(
                messages = uiState.messages - uiState.assistantType,
                isLoading = false,
            )
        }
    }

    private fun handleOnIsSummarizationNeededCheck(isChecked: Boolean) {
        updateUiState { copy(isSummarizationNeeded = isChecked) }
    }

    private fun sendMessage(text: String) {
        sendMessageJob?.cancel()
        sendMessageJob = launch(
            onError = {
                Log.e("OpenAiRepository", it.stackTraceToString())
                updateUiState { copy(isLoading = false) }
            },
        ) {
            updateUiState { copy(isLoading = true) }

            val userMessage = ChatMessageEntity(
                id = System.nanoTime().toString(),
                role = ChatRoleEntity.User,
                data = ChatMessageDataEntity(
                    text = text,
                    aiModel = uiState.aiModel,
                ),
                tokenData = TokenDataEntity(),
            )
            val updatedByUserMessages = uiState.currentMessages + userMessage
            updateUiState {
                messagesChange(
                    messages = uiState.messages + (uiState.assistantType to updatedByUserMessages),
                    isLoading = true,
                )
            }

            val lastSummarizationIndex = updatedByUserMessages.indexOfLast {
                it.role == ChatRoleEntity.Summarization
            }.coerceAtLeast(0)
            val fromLastSummarizationMessages =
                updatedByUserMessages.subList(lastSummarizationIndex, updatedByUserMessages.size)
            val summarizationMessage = if (uiState.isSummarizationNeeded) {
                summariseMessages(fromLastSummarizationMessages)
            } else null

            val assistantMessage = sendInput(
                assistantType = uiState.assistantType,
                messages = fromLastSummarizationMessages,
            )
            val updatedByAssistantMessages =
                updatedByUserMessages + listOfNotNull(summarizationMessage, assistantMessage)
            updateUiState {
                messagesChange(
                    messages = uiState.messages + (uiState.assistantType to updatedByAssistantMessages),
                    isLoading = false,
                )
            }
        }
    }

    private suspend fun summariseMessages(
        messages: List<ChatMessageEntity>,
    ): ChatMessageEntity? {
        if (messages.isEmpty()) return null
        val assistantMessageCount = messages.count { it.role == ChatRoleEntity.Assistant }
        val isSummarizationThresholdReached = if (assistantMessageCount > 0 && uiState.summarizationThreshold > 0) {
            (assistantMessageCount % uiState.summarizationThreshold) == 0
        } else false
        if (isSummarizationThresholdReached.not()) return null

        return sendInput(
            assistantType = uiState.assistantType,
            messages = messages,
            isSummarization = true,
        )
    }

    private suspend fun sendInput(
        assistantType: AssistantType,
        messages: List<ChatMessageEntity>,
        isSummarization: Boolean = false,
    ): ChatMessageEntity {
        return when (uiState.aiModel) {
            AiModelEntity.GPT_5,
            AiModelEntity.GPT_5_MINI,
            AiModelEntity.GPT_5_NANO,
                -> {
                openAiRepository.sendInput(
                    assistantType = assistantType,
                    input = messages,
                    aiModel = uiState.aiModel,
                    aiTemperature = uiState.aiTemperature,
                    isSummarization = isSummarization,
                )
            }

            AiModelEntity.ZAI_ORG,
            AiModelEntity.DEEP_SEEK,
            AiModelEntity.KIMI_K2,
                -> {
                hfRepository.sendInput(
                    assistantType = assistantType,
                    input = messages,
                    aiModel = uiState.aiModel,
                    aiTemperature = uiState.aiTemperature,
                    isSummarization = isSummarization,
                )
            }
        }
    }
}
