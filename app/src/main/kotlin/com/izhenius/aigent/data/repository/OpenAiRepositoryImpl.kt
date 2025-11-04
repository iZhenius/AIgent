package com.izhenius.aigent.data.repository

import com.izhenius.aigent.data.dto.OpenAiResponseDto
import com.izhenius.aigent.data.network.OpenAiApi
import com.izhenius.aigent.domain.repository.OpenAiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.json.JSONObject

class OpenAiRepositoryImpl(
    private val api: OpenAiApi,
) : OpenAiRepository {

    override suspend fun sendInput(input: String): String = withContext(Dispatchers.IO) {
        val bodyJson = JSONObject()
            .put("model", "gpt-5-nano")
            .put("input", input)
            .toString()

        val request = api.buildRequest(bodyJson)
        api.execute(request).use { response: Response ->
            if (!response.isSuccessful) {
                val errorBody = response.body.string()
                throw IllegalStateException("OpenAI error ${response.code}: $errorBody")
            }
            val responseBody = response.body.string()
            val jsonResponse = JSONObject(responseBody)
            val responseDto = OpenAiResponseDto.fromJson(jsonResponse)
            val extractedText = responseDto.extractText()
            
            if (extractedText.isEmpty()) {
                throw IllegalStateException("No text content found in OpenAI response")
            }
            
            return@withContext extractedText
        }
    }
}
