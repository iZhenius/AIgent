package com.izhenius.aigent.data.mapper

import com.izhenius.aigent.data.dto.MessageOutputDto
import com.izhenius.aigent.data.dto.OutputTextContentDto
import com.izhenius.aigent.domain.model.ChatMessageDataEntity
import com.izhenius.aigent.domain.model.ChatMessageEntity
import com.izhenius.aigent.domain.model.ChatRoleEntity
import org.json.JSONObject

fun MessageOutputDto.toChatMessageEntity(): ChatMessageEntity {
    val textContent = content
        .filterIsInstance<OutputTextContentDto>()
        .firstOrNull()
        ?: throw IllegalStateException("No OutputTextContentDto found in MessageOutputDto")

    // Parse JSON from text field according to the schema
    val jsonText = textContent.text
    val dataJson = JSONObject(jsonText)

    val data = ChatMessageDataEntity(
        text = dataJson.getString("text"),
        aiModel = dataJson.getString("ai_model"),
        tokens = dataJson.getString("tokens"),
    )

    val role = when (role?.lowercase()) {
        "user" -> ChatRoleEntity.User
        else -> ChatRoleEntity.Assistant
    }

    return ChatMessageEntity(
        id = id,
        role = role,
        data = data,
    )
}
