package com.izhenius.aigent.domain.model

data class TokenDataEntity(
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val totalTokens: Int = 0,
    val inputCost: Double = 0.0,
    val outputCost: Double = 0.0,
    val totalCost: Double = 0.0,
)
