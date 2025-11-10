package com.izhenius.aigent.domain.model

enum class AiTemperatureEntity {
    LOW, MEDIUM, HIGH;

    val reasoningEffort: String
        get() = when (this) {
            LOW -> "minimal"
            MEDIUM -> "medium"
            HIGH -> "high"
        }

    val textVerbosity: String
        get() = when (this) {
            LOW -> "low"
            MEDIUM -> "medium"
            HIGH -> "high"
        }
}

