// NEW FILE
// app/src/main/java/com/marketplace/dto/AppLanguage.kt
package com.marketplace.dto

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
        val all: List<AppLanguage> get() = entries.toList()
    }
}
