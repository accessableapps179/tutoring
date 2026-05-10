// NEW FILE
// src/main/kotlin/com/marketplace/domain/AppLanguage.kt
package com.marketplace.domain

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Russian"),
    VIETNAMESE("vi", "Vietnamese"),
    MANDARIN("zh", "Chinese (Mandarin)"),
    THAI("th", "Thai"),
    INDONESIAN("id", "Indonesian"),
    KOREAN("ko", "Korean"),
    JAPANESE("ja", "Japanese");

    companion object {
        fun fromCode(code: String): AppLanguage? = entries.firstOrNull { it.code == code }
        val allCodes: List<String> get() = entries.map { it.code }
    }
}
