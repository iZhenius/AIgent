package com.izhenius.aigent.domain.model

enum class AiModelEntity {
    GPT_5,
    GPT_5_MINI,
    GPT_5_NANO,
    ZAI_ORG,
    DEEP_SEEK,
    KIMI_K2,
    ;

    val apiValue: String
        get() = when (this) {
            GPT_5 -> "gpt-5"
            GPT_5_MINI -> "gpt-5-mini"
            GPT_5_NANO -> "gpt-5-nano"
            ZAI_ORG -> "zai-org/GLM-4.5"
            DEEP_SEEK -> "deepseek-ai/DeepSeek-V3.1"
            KIMI_K2 -> "moonshotai/Kimi-K2-Instruct"
        }
}

