package com.izhenius.aigent.data.repository

import android.util.Log
import com.izhenius.aigent.data.dto.MessageOutputDto
import com.izhenius.aigent.data.dto.OpenAiResponseDto
import com.izhenius.aigent.data.mapper.coreInstructions
import com.izhenius.aigent.data.mapper.summarizationInstructions
import com.izhenius.aigent.data.mapper.toChatMessageEntity
import com.izhenius.aigent.data.mapper.toInstructions
import com.izhenius.aigent.data.network.OpenAiApi
import com.izhenius.aigent.domain.model.AiModelEntity
import com.izhenius.aigent.domain.model.AiTemperatureEntity
import com.izhenius.aigent.domain.model.AssistantType
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

    override suspend fun sendInput(
        assistantType: AssistantType,
        input: List<ChatMessageEntity>,
        aiModel: AiModelEntity,
        aiTemperature: AiTemperatureEntity,
        isSummarization: Boolean,
    ): ChatMessageEntity = withContext(Dispatchers.IO) {
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

        val properties = JSONObject()
            .put("text", textProperty)
            .put("ai_model", aiModelProperty)

        val requiredArray = JSONArray()
            .put("text")
            .put("ai_model")

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
            .put("verbosity", aiTemperature.textVerbosity)

        val reasoningObject = JSONObject()
            .put("effort", aiTemperature.reasoningEffort)

        val bodyJson = JSONObject()
            .put("model", aiModel.id)
            .put(
                "instructions",
                if (isSummarization) {
                    summarizationInstructions
                } else {
                    "$coreInstructions\n${assistantType.toInstructions()}"
                },
            )
            .put("input", inputArray)
            .put("text", textObject)
            .put("reasoning", reasoningObject)
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

            return@withContext messageOutput.toChatMessageEntity(
                isSummarization = isSummarization,
                aiModel = aiModel,
                usage = responseDto.usage,
            )
        }
    }
}
