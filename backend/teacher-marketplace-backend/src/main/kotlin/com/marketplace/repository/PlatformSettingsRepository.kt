// IntelliJ
// src/main/kotlin/com/marketplace/repository/PlatformSettingsRepository.kt
package com.marketplace.repository

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class PlatformSettingsRepository {

    fun get(key: String, default: String): String = transaction {
        PlatformSettingsTable
            .select { PlatformSettingsTable.key eq key }
            .firstOrNull()
            ?.get(PlatformSettingsTable.value)
            ?: default
    }

    fun set(key: String, value: String): Unit = transaction {
        val exists = PlatformSettingsTable
            .select { PlatformSettingsTable.key eq key }
            .firstOrNull() != null
        if (exists) {
            PlatformSettingsTable.update({ PlatformSettingsTable.key eq key }) {
                it[PlatformSettingsTable.value] = value
            }
        } else {
            PlatformSettingsTable.insert {
                it[PlatformSettingsTable.key]   = key
                it[PlatformSettingsTable.value] = value
            }
        }
    }

    // Commission is stored and returned as a percentage (e.g. 10.0 means 10%)
    fun getCommissionRate(): Double =
        get("commission_rate", "10.0").toDoubleOrNull() ?: 10.0

    fun setCommissionRate(percent: Double) =
        set("commission_rate", percent.toString())
}
