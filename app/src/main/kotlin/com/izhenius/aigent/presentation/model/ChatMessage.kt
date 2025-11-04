package com.izhenius.aigent.presentation.model

data class ChatMessage(
    val id: Long,
    val role: ChatRole,
    val text: String,
)
