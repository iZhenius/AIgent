package com.izhenius.aigent.domain.model

enum class AiModelEntity(
    val id: String,
    val inputTokenPrice: Double,
    val outputTokenPrice: Double,
) {
    GPT_5(id = "gpt-5", inputTokenPrice = 1.25, outputTokenPrice = 10.00),
    GPT_5_MINI(id = "gpt-5-mini", inputTokenPrice = 0.25, outputTokenPrice = 2.00),
    GPT_5_NANO(id = "gpt-5-nano", inputTokenPrice = 0.05, outputTokenPrice = 0.40),
    ZAI_ORG(id = "zai-org/GLM-4.5", inputTokenPrice = 0.6, outputTokenPrice = 1.8),
    DEEP_SEEK(id = "deepseek-ai/DeepSeek-V3.1:novita", inputTokenPrice = 0.27, outputTokenPrice = 1.0),
    KIMI_K2(id = "moonshotai/Kimi-K2-Instruct:novita", inputTokenPrice = 0.57, outputTokenPrice = 2.3),
    ;
}
