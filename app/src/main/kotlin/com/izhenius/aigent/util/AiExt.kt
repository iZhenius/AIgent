package com.izhenius.aigent.util

import com.izhenius.aigent.domain.model.AiModelEntity

private const val defaultPriceByTokenCount: Double = 1_000_000.0

fun AiModelEntity.calculateInputTokenCost(
    inputTokens: Int,
): Double {
    return inputTokenPrice / defaultPriceByTokenCount * inputTokens
}

fun AiModelEntity.calculateOutputTokenCost(
    outputTokens: Int,
): Double {
    return outputTokenPrice / defaultPriceByTokenCount * outputTokens
}

fun AiModelEntity.calculateTotalTokenCost(
    inputTokens: Int,
    outputTokens: Int,
): Double {
    return calculateInputTokenCost(inputTokens) + calculateOutputTokenCost(outputTokens)
}
