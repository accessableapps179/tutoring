package com.marketplace.domain

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val role: String,
    val name: String = "",
    val fcmToken: String? = null
)