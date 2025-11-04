package com.izhenius.aigent.data.dto

import org.json.JSONObject

data class OpenAiResponseDto(
    val id: String,
    val objectType: String,
    val createdAt: Long,
    val status: String,
    val background: Boolean,
    val billing: BillingDto?,
    val error: String?,
    val incompleteDetails: String?,
    val instructions: String?,
    val maxOutputTokens: Int?,
    val maxToolCalls: Int?,
    val model: String,
    val output: List<OutputItemDto>,
    val parallelToolCalls: Boolean,
    val previousResponseId: String?,
    val promptCacheKey: String?,
    val promptCacheRetention: Int?,
    val reasoning: ReasoningDto?,
    val safetyIdentifier: String?,
    val serviceTier: String,
    val store: Boolean,
    val temperature: Double,
    val text: TextFormatDto?,
    val toolChoice: String,
    val tools: List<String>,
    val topLogprobs: Int,
    val topP: Double,
    val truncation: String,
    val usage: UsageDto?,
    val user: String?,
    val metadata: Map<String, Any>,
) {
    companion object {
        fun fromJson(json: JSONObject): OpenAiResponseDto {
            return OpenAiResponseDto(
                id = json.getString("id"),
                objectType = json.getString("object"),
                createdAt = json.getLong("created_at"),
                status = json.getString("status"),
                background = json.optBoolean("background", false),
                billing = json.optJSONObject("billing")?.let { BillingDto.fromJson(it) },
                error = json.optString("error").takeIf { !it.isNullOrEmpty() && it != "null" },
                incompleteDetails = json.optString("incomplete_details").takeIf { !it.isNullOrEmpty() && it != "null" },
                instructions = json.optString("instructions").takeIf { !it.isNullOrEmpty() && it != "null" },
                maxOutputTokens = json.optInt("max_output_tokens").takeIf { it != 0 },
                maxToolCalls = json.optInt("max_tool_calls").takeIf { it != 0 },
                model = json.getString("model"),
                output = json.optJSONArray("output")?.let { array ->
                    (0 until array.length()).map { OutputItemDto.fromJson(array.getJSONObject(it)) }
                } ?: emptyList(),
                parallelToolCalls = json.optBoolean("parallel_tool_calls", false),
                previousResponseId = json.optString("previous_response_id")
                    .takeIf { !it.isNullOrEmpty() && it != "null" },
                promptCacheKey = json.optString("prompt_cache_key").takeIf { !it.isNullOrEmpty() && it != "null" },
                promptCacheRetention = json.optInt("prompt_cache_retention").takeIf { it != 0 },
                reasoning = json.optJSONObject("reasoning")?.let { ReasoningDto.fromJson(it) },
                safetyIdentifier = json.optString("safety_identifier").takeIf { !it.isNullOrEmpty() && it != "null" },
                serviceTier = json.optString("service_tier", "default"),
                store = json.optBoolean("store", true),
                temperature = json.optDouble("temperature", 1.0),
                text = json.optJSONObject("text")?.let { TextFormatDto.fromJson(it) },
                toolChoice = json.optString("tool_choice", "auto"),
                tools = json.optJSONArray("tools")?.let { array ->
                    (0 until array.length()).map { array.getString(it) }
                } ?: emptyList(),
                topLogprobs = json.optInt("top_logprobs", 0),
                topP = json.optDouble("top_p", 1.0),
                truncation = json.optString("truncation", "disabled"),
                usage = json.optJSONObject("usage")?.let { UsageDto.fromJson(it) },
                user = json.optString("user").takeIf { !it.isNullOrEmpty() && it != "null" },
                metadata = json.optJSONObject("metadata")?.let { obj ->
                    obj.keys().asSequence().associateWith { obj.get(it) }
                } ?: emptyMap(),
            )
        }
    }

    /**
     * Extracts the text content from the response.
     * Looks for output items of type "message" with content items of type "output_text".
     */
    fun extractText(): String {
        return output.filterIsInstance<MessageOutputDto>()
            .flatMap { it.content }
            .filterIsInstance<OutputTextContentDto>()
            .joinToString("\n", transform = { it.text })
            .takeIf { it.isNotEmpty() }
            .orEmpty()
    }
}

data class BillingDto(
    val payer: String,
) {
    companion object {
        fun fromJson(json: JSONObject): BillingDto {
            return BillingDto(
                payer = json.getString("payer"),
            )
        }
    }
}

sealed class OutputItemDto(
    open val id: String,
    open val type: String,
) {
    companion object {
        fun fromJson(json: JSONObject): OutputItemDto {
            val type = json.getString("type")
            return when (type) {
                "reasoning" -> ReasoningOutputDto.fromJson(json)
                "message" -> MessageOutputDto.fromJson(json)
                else -> UnknownOutputDto.fromJson(json)
            }
        }
    }
}

data class ReasoningOutputDto(
    override val id: String,
    override val type: String,
    val summary: List<String>,
) : OutputItemDto(id, type) {
    companion object {
        fun fromJson(json: JSONObject): ReasoningOutputDto {
            return ReasoningOutputDto(
                id = json.getString("id"),
                type = json.getString("type"),
                summary = json.optJSONArray("summary")?.let { array ->
                    (0 until array.length()).map { array.getString(it) }
                } ?: emptyList(),
            )
        }
    }
}

data class MessageOutputDto(
    override val id: String,
    override val type: String,
    val status: String?,
    val content: List<ContentItemDto>,
    val role: String?,
) : OutputItemDto(id, type) {
    companion object {
        fun fromJson(json: JSONObject): MessageOutputDto {
            return MessageOutputDto(
                id = json.getString("id"),
                type = json.getString("type"),
                status = json.optString("status").takeIf { !it.isNullOrEmpty() && it != "null" },
                content = json.optJSONArray("content")?.let { array ->
                    (0 until array.length()).map { ContentItemDto.fromJson(array.getJSONObject(it)) }
                } ?: emptyList(),
                role = json.optString("role").takeIf { !it.isNullOrEmpty() && it != "null" },
            )
        }
    }
}

data class UnknownOutputDto(
    override val id: String,
    override val type: String,
) : OutputItemDto(id, type) {
    companion object {
        fun fromJson(json: JSONObject): UnknownOutputDto {
            return UnknownOutputDto(
                id = json.getString("id"),
                type = json.getString("type"),
            )
        }
    }
}

sealed class ContentItemDto(
    open val type: String,
) {
    companion object {
        fun fromJson(json: JSONObject): ContentItemDto {
            val type = json.getString("type")
            return when (type) {
                "output_text" -> OutputTextContentDto.fromJson(json)
                else -> UnknownContentDto.fromJson(json)
            }
        }
    }
}

data class OutputTextContentDto(
    override val type: String,
    val text: String,
    val annotations: List<String>,
    val logprobs: List<String>,
) : ContentItemDto(type) {
    companion object {
        fun fromJson(json: JSONObject): OutputTextContentDto {
            return OutputTextContentDto(
                type = json.getString("type"),
                text = json.getString("text"),
                annotations = json.optJSONArray("annotations")?.let { array ->
                    (0 until array.length()).map { array.getString(it) }
                } ?: emptyList(),
                logprobs = json.optJSONArray("logprobs")?.let { array ->
                    (0 until array.length()).map { array.getString(it) }
                } ?: emptyList(),
            )
        }
    }
}

data class UnknownContentDto(
    override val type: String,
) : ContentItemDto(type) {
    companion object {
        fun fromJson(json: JSONObject): UnknownContentDto {
            return UnknownContentDto(
                type = json.getString("type"),
            )
        }
    }
}

data class ReasoningDto(
    val effort: String?,
    val summary: String?,
) {
    companion object {
        fun fromJson(json: JSONObject): ReasoningDto {
            return ReasoningDto(
                effort = json.optString("effort").takeIf { !it.isNullOrEmpty() && it != "null" },
                summary = json.optString("summary").takeIf { !it.isNullOrEmpty() && it != "null" },
            )
        }
    }
}

data class TextFormatDto(
    val format: TextFormatTypeDto,
    val verbosity: String?,
) {
    companion object {
        fun fromJson(json: JSONObject): TextFormatDto {
            return TextFormatDto(
                format = json.optJSONObject("format")?.let { TextFormatTypeDto.fromJson(it) }
                    ?: TextFormatTypeDto("text"),
                verbosity = json.optString("verbosity").takeIf { !it.isNullOrEmpty() && it != "null" },
            )
        }
    }
}

data class TextFormatTypeDto(
    val type: String,
) {
    companion object {
        fun fromJson(json: JSONObject): TextFormatTypeDto {
            return TextFormatTypeDto(
                type = json.getString("type"),
            )
        }
    }
}

data class UsageDto(
    val inputTokens: Int,
    val inputTokensDetails: InputTokensDetailsDto?,
    val outputTokens: Int,
    val outputTokensDetails: OutputTokensDetailsDto?,
    val totalTokens: Int,
) {
    companion object {
        fun fromJson(json: JSONObject): UsageDto {
            return UsageDto(
                inputTokens = json.getInt("input_tokens"),
                inputTokensDetails = json.optJSONObject("input_tokens_details")?.let {
                    InputTokensDetailsDto.fromJson(it)
                },
                outputTokens = json.getInt("output_tokens"),
                outputTokensDetails = json.optJSONObject("output_tokens_details")?.let {
                    OutputTokensDetailsDto.fromJson(it)
                },
                totalTokens = json.getInt("total_tokens"),
            )
        }
    }
}

data class InputTokensDetailsDto(
    val cachedTokens: Int,
) {
    companion object {
        fun fromJson(json: JSONObject): InputTokensDetailsDto {
            return InputTokensDetailsDto(
                cachedTokens = json.optInt("cached_tokens", 0),
            )
        }
    }
}

data class OutputTokensDetailsDto(
    val reasoningTokens: Int,
) {
    companion object {
        fun fromJson(json: JSONObject): OutputTokensDetailsDto {
            return OutputTokensDetailsDto(
                reasoningTokens = json.optInt("reasoning_tokens", 0),
            )
        }
    }
}
