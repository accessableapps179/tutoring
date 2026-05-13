// IntelliJ
// src/main/kotlin/com/marketplace/infrastructure/DatabaseFactory.kt
package com.marketplace.infrastructure

import com.marketplace.repository.AvailabilityOverrideTable
import com.marketplace.repository.BookingTable
import com.marketplace.repository.ContactTable
import com.marketplace.repository.FcmTokenTable
import com.marketplace.repository.LedgerTable
import com.marketplace.repository.MessageTable
import com.marketplace.repository.PaymentCardTable
import com.marketplace.repository.PlatformSettingsTable
import com.marketplace.repository.PlatonicSlotTable
import com.marketplace.repository.TeacherHourRangeTable
import com.marketplace.repository.TeacherTable
import com.marketplace.repository.TrialResultTable
import com.marketplace.repository.UserTable
import com.marketplace.repository.WeeklySlotTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/marketplace",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postal1"
        )
        createTables()
    }

    fun nuke() = transaction {
        SchemaUtils.drop(
            BookingTable,
            WeeklySlotTable,
            AvailabilityOverrideTable,
            TeacherHourRangeTable,
            PlatonicSlotTable,
            ContactTable,
            MessageTable,
            PaymentCardTable,
            FcmTokenTable,
            TrialResultTable,
            LedgerTable,
            PlatformSettingsTable,
            UserTable,
            TeacherTable
        )
        createTables()
    }

    private fun createTables() = transaction {
        SchemaUtils.create(
            TeacherTable,
            UserTable,
            BookingTable,
            WeeklySlotTable,
            AvailabilityOverrideTable,
            TeacherHourRangeTable,
            PlatonicSlotTable,
            ContactTable,
            MessageTable,
            PaymentCardTable,
            FcmTokenTable,
            TrialResultTable,
            LedgerTable,
            PlatformSettingsTable
        )
        SchemaUtils.createMissingTablesAndColumns(
            TeacherTable,
            UserTable,
            BookingTable,
            WeeklySlotTable,
            AvailabilityOverrideTable,
            TeacherHourRangeTable,
            PlatonicSlotTable,
            ContactTable,
            MessageTable,
            PaymentCardTable,
            FcmTokenTable,
            TrialResultTable,
            LedgerTable,
            PlatformSettingsTable
        )
    }
}