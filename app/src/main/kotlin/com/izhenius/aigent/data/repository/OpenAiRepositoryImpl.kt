package com.izhenius.aigent.data.repository

import android.util.Log
import com.izhenius.aigent.data.dto.MessageOutputDto
import com.izhenius.aigent.data.dto.OpenAiResponseDto
import com.izhenius.aigent.data.mapper.toChatMessageEntity
import com.izhenius.aigent.data.network.OpenAiApi
import com.izhenius.aigent.domain.model.ChatMessageEntity
import com.izhenius.aigent.domain.repository.OpenAiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class OpenAiRepositoryImpl(
    private val api: OpenAiApi,
) : OpenAiRepository {

    override suspend fun sendInput(input: List<ChatMessageEntity>): ChatMessageEntity = withContext(Dispatchers.IO) {
        val inputArray = JSONArray()
        input.forEach { message ->
            val messageObject = JSONObject()
            messageObject.put("role", message.role.id)
            messageObject.put("content", message.data.text)
            inputArray.put(messageObject)
        }

        // Build the text format schema
        val textProperty = JSONObject().put("type", "string")
        val aiModelProperty = JSONObject().put("type", "string")
        val tokensProperty = JSONObject().put("type", "string")

        val properties = JSONObject()
            .put("text", textProperty)
            .put("ai_model", aiModelProperty)
            .put("tokens", tokensProperty)

        val requiredArray = JSONArray()
            .put("text")
            .put("ai_model")
            .put("tokens")

        val schema = JSONObject()
            .put("type", "object")
            .put("properties", properties)
            .put("required", requiredArray)
            .put("additionalProperties", false)

        val format = JSONObject()
            .put("type", "json_schema")
            .put("name", "chat_message")
            .put("strict", true)
            .put("schema", schema)

        val textObject = JSONObject()
            .put("format", format)

        val bodyJson = JSONObject()
            .put("model", "gpt-5-nano")
            .put(
                "instructions",
                "You are a helpful assistant from Belarus." +
                        "You can answer sometimes in belarusian or russian" +
                        "You should convert responses into the given json schema." +
                        "property text is your original answer, property ai_model is current chat-gpt model you has used for answer, property tokens is total tokens used for answering",
            )
            .put("input", inputArray)
            .put("text", textObject)
            .toString()

        val request = api.buildRequest(bodyJson)
        api.execute(request).use { response: Response ->
            if (!response.isSuccessful) {
                val errorBody = response.body.string()
                throw IllegalStateException("OpenAI error ${response.code}: $errorBody")
            }
            val responseBody = response.body.string()
            Log.e("OpenAiResponse", responseBody)
            val jsonResponse = JSONObject(responseBody)
            val responseDto = OpenAiResponseDto.fromJson(jsonResponse)

            val messageOutput = responseDto.output
                .filterIsInstance<MessageOutputDto>()
                .firstOrNull()
                ?: throw IllegalStateException("No MessageOutputDto found in OpenAI response")

            return@withContext messageOutput.toChatMessageEntity()
        }
    }
}
