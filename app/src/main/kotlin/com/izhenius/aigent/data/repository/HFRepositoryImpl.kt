package com.izhenius.aigent.data.repository

import android.util.Log
import com.izhenius.aigent.data.dto.MessageOutputDto
import com.izhenius.aigent.data.dto.OpenAiResponseDto
import com.izhenius.aigent.data.mapper.coreInstructions
import com.izhenius.aigent.data.mapper.toChatMessageEntity
import com.izhenius.aigent.data.mapper.toInstructions
import com.izhenius.aigent.data.network.HFApi
import com.izhenius.aigent.domain.model.AiModelEntity
import com.izhenius.aigent.domain.model.AiTemperatureEntity
import com.izhenius.aigent.domain.model.AssistantType
import com.izhenius.aigent.domain.model.ChatMessageEntity
import com.izhenius.aigent.domain.repository.HFRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class HFRepositoryImpl(
    private val api: HFApi,
) : HFRepository {

    override suspend fun sendInput(
        assistantType: AssistantType,
        input: List<ChatMessageEntity>,
        aiModel: AiModelEntity,
        aiTemperature: AiTemperatureEntity,
    ): ChatMessageEntity = withContext(Dispatchers.IO) {
        // Build input array with role and content objects
        val inputArray = JSONArray()
        input.forEach { message ->
            val messageObject = JSONObject()
            messageObject.put("role", message.role.id)
            messageObject.put("content", message.data.text)
            inputArray.put(messageObject)
        }

        // Build the same json_schema "chat_message" as used in OpenAiRepositoryImpl
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
            .put("strict", true)

        val jsonSchema = JSONObject()
            .put("name", "chat_message")
            .put("schema", schema)
            .put("strict", true)

        val responseFormat = JSONObject()
            .put("type", "json_schema")
            .put("json_schema", jsonSchema)

        val reasoningObject = JSONObject()
            .put("effort", aiTemperature.textVerbosity)

        // Build request body according to HF API format
        val bodyJson = JSONObject()
            .put("model", aiModel.apiValue)
            .put(
                "instructions",
                "$coreInstructions\n${assistantType.toInstructions()}",
            )
            .put("input", inputArray)
            .put("reasoning", reasoningObject)
            .put("response_format", responseFormat)
            .toString()

        val request = api.buildRequest(bodyJson)
        api.execute(request).use { response: Response ->
            if (!response.isSuccessful) {
                val errorBody = response.body.string()
                throw IllegalStateException("Hugging Face error ${response.code}: $errorBody")
            }
            val responseBody = response.body.string()
            Log.e("HFResponse", responseBody)
            val jsonResponse = JSONObject(responseBody)

            val responseDto = OpenAiResponseDto.fromJson(jsonResponse)
            val messageOutput = responseDto.output
                .filterIsInstance<MessageOutputDto>()
                .firstOrNull()
                ?: throw IllegalStateException("No MessageOutputDto found in HF response")

            return@withContext messageOutput.toChatMessageEntity(responseDto.usage)
        }
    }
}

