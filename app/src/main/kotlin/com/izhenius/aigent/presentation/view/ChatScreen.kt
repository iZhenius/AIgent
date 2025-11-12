package com.izhenius.aigent.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.izhenius.aigent.R
import com.izhenius.aigent.di.ChatViewModelFactory
import com.izhenius.aigent.domain.model.AiModelEntity
import com.izhenius.aigent.domain.model.AiTemperatureEntity
import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageEntity
import com.izhenius.aigent.domain.model.ChatRoleEntity
import com.izhenius.aigent.presentation.mvi.ChatUiAction
import com.izhenius.aigent.presentation.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory()),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val input = rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.currentMessages.size, uiState.isLoading, uiState.assistantType) {
        val itemCount = uiState.currentMessages.size + if (uiState.isLoading) 1 else 0
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBarContent(uiState.assistantType, viewModel::onUiAction)
        },
        bottomBar = {
            BottomBarContent(input, uiState.aiModel, uiState.aiTemperature, viewModel::onUiAction)
        },
    ) { paddings ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false,
            contentPadding = PaddingValues(16.dp),
        ) {
            items(uiState.currentMessages) { message ->
                MessageBubble(message = message)
            }
            if (uiState.isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun TopAppBarContent(
    assistantType: AssistantType,
    onUiAction: (ChatUiAction) -> Unit,
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                )
                {
                    Text(
                        text = "Chat",
                        textAlign = TextAlign.Center,
                    )
                    IconButton(
                        onClick = {
                            onUiAction(ChatUiAction.OnClearChat)
                        },
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.delete_forever_24px),
                            contentDescription = "Clear chat",
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = assistantType == AssistantType.BUDDY,
                        onClick = {
                            onUiAction(
                                ChatUiAction.OnChangeAssistantType(AssistantType.BUDDY),
                            )
                        },
                        label = { Text("Buddy") },
                    )
                    FilterChip(
                        selected = assistantType == AssistantType.SPECIALIST,
                        onClick = {
                            onUiAction(
                                ChatUiAction.OnChangeAssistantType(AssistantType.SPECIALIST),
                            )
                        },
                        label = { Text("Specialist") },
                    )
                    FilterChip(
                        selected = assistantType == AssistantType.EXPERTS,
                        onClick = {
                            onUiAction(
                                ChatUiAction.OnChangeAssistantType(AssistantType.EXPERTS),
                            )
                        },
                        label = { Text("Experts") },
                    )
                }
            }
        },
    )
}

@Composable
private fun BottomBarContent(
    input: MutableState<String>,
    aiModel: AiModelEntity,
    aiTemperature: AiTemperatureEntity,
    onUiAction: (ChatUiAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Model:")
            FilterChip(
                selected = aiModel == AiModelEntity.ZAI_ORG,
                onClick = {
                    onUiAction(
                        ChatUiAction.OnChangeModel(AiModelEntity.ZAI_ORG),
                    )
                },
                label = { Text(AiModelEntity.ZAI_ORG.name) },
            )
            FilterChip(
                selected = aiModel == AiModelEntity.DEEP_SEEK,
                onClick = {
                    onUiAction(
                        ChatUiAction.OnChangeModel(AiModelEntity.DEEP_SEEK),
                    )
                },
                label = { Text(AiModelEntity.DEEP_SEEK.name) },
            )
            FilterChip(
                selected = aiModel == AiModelEntity.KIMI_K2,
                onClick = {
                    onUiAction(
                        ChatUiAction.OnChangeModel(AiModelEntity.KIMI_K2),
                    )
                },
                label = { Text(AiModelEntity.KIMI_K2.name) },
            )
        }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Temperature:")
            FilterChip(
                selected = aiTemperature == AiTemperatureEntity.LOW,
                onClick = {
                    onUiAction(
                        ChatUiAction.OnChangeTemperatureLevel(AiTemperatureEntity.LOW),
                    )
                },
                label = { Text("Low") },
            )
            FilterChip(
                selected = aiTemperature == AiTemperatureEntity.MEDIUM,
                onClick = {
                    onUiAction(
                        ChatUiAction.OnChangeTemperatureLevel(AiTemperatureEntity.MEDIUM),
                    )
                },
                label = { Text("Medium") },
            )
            FilterChip(
                selected = aiTemperature == AiTemperatureEntity.HIGH,
                onClick = {
                    onUiAction(
                        ChatUiAction.OnChangeTemperatureLevel(AiTemperatureEntity.HIGH),
                    )
                },
                label = { Text("High") },
            )
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .imePadding(),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = input.value,
                onValueChange = { input.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Type a message…") },
            )
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = {
                    val text = input.value.trim()
                    if (text.isNotEmpty()) {
                        onUiAction(ChatUiAction.OnSendMessage(text))
                        input.value = ""
                    }
                },
            ) { Text(text = "Send") }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessageEntity) {
    val isUser = message.role == ChatRoleEntity.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        val cardColors = CardDefaults.cardColors(
            containerColor = if (isUser) {
                CardDefaults.elevatedCardColors().containerColor
            } else {
                CardDefaults.cardColors().containerColor
            },
        )
        Card(
            colors = cardColors,
            modifier = Modifier.fillMaxWidth(0.85f),
        ) {
            Text(
                text = if (isUser) {
                    "YOU"
                } else {
                    "${message.data.aiModel.uppercase()} (tokens:${message.data.tokens})"
                },
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp),
                color = cardColors.contentColor.copy(alpha = 0.38f),
                fontStyle = FontStyle.Italic,
            )
            SelectionContainer {
                Text(
                    text = message.data.text,
                    modifier = Modifier.padding(12.dp),
                    color = cardColors.contentColor,
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "Typing...",
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(0.85f),
            color = Color.Gray,
            fontStyle = FontStyle.Italic,
        )
    }
}
