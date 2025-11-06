package com.izhenius.aigent.domain.model

data class ChatMessageEntity(
    val id: String,
    val role: ChatRoleEntity,
    val data: ChatMessageDataEntity,
)
