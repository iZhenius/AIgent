package com.izhenius.aigent.domain.model

enum class AiModelEntity {
    GPT_5,
    GPT_5_MINI,
    GPT_5_NANO;

    val apiValue: String
        get() = when (this) {
            GPT_5 -> "gpt-5"
            GPT_5_MINI -> "gpt-5-mini"
            GPT_5_NANO -> "gpt-5-nano"
        }
}

