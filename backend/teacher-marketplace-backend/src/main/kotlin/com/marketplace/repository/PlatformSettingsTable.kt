// IntelliJ
// src/main/kotlin/com/marketplace/repository/PlatformSettingsTable.kt
package com.marketplace.repository

import org.jetbrains.exposed.sql.Table

object PlatformSettingsTable : Table("platform_settings") {
    val key   = varchar("key", 50)
    val value = varchar("value", 100)
    override val primaryKey = PrimaryKey(key)
}
