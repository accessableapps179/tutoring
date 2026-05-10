package com.marketplace.api

import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    val token: String
)