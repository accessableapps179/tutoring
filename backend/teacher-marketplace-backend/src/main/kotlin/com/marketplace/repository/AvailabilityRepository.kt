package com.marketplace.repository

import com.marketplace.domain.AvailabilityOverride
import com.marketplace.domain.PlatonicSlot
import com.marketplace.domain.TeacherHourRange
import com.marketplace.domain.WeeklySlot
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class AvailabilityRepository {

    // ─── Weekly Slots ───────────────────────────────────────

    fun getWeeklySlots(teacherId: String): List<WeeklySlot> = transaction {
        WeeklySlotTable.select { WeeklySlotTable.teacherId eq teacherId }
            .map { mapRowToWeeklySlot(it) }
    }

    fun saveWeeklySlot(slot: WeeklySlot): WeeklySlot = transaction {
        val exists = WeeklySlotTable.select {
            (WeeklySlotTable.teacherId eq slot.teacherId) and
                    (WeeklySlotTable.dayOfWeek eq slot.dayOfWeek) and
                    (WeeklySlotTable.hour eq slot.hour)
        }.count() > 0

        if (!exists) {
            WeeklySlotTable.insert {
                it[id] = slot.id
                it[teacherId] = slot.teacherId
                it[dayOfWeek] = slot.dayOfWeek
                it[hour] = slot.hour
            }
        }
        slot
    }

    fun deleteWeeklySlot(teacherId: String, dayOfWeek: Int, hour: Double): Boolean = transaction {
        WeeklySlotTable.deleteWhere {
            (WeeklySlotTable.teacherId eq teacherId) and
                    (WeeklySlotTable.dayOfWeek eq dayOfWeek) and
                    (WeeklySlotTable.hour eq hour)
        } > 0
    }

    // ─── Overrides ──────────────────────────────────────────

    fun getOverridesForTeacher(teacherId: String): List<AvailabilityOverride> = transaction {
        AvailabilityOverrideTable.select {
            AvailabilityOverrideTable.teacherId eq teacherId
        }.map { mapRowToOverride(it) }
    }

    fun getOverridesForDate(teacherId: String, date: String): List<AvailabilityOverride> = transaction {
        AvailabilityOverrideTable.select {
            (AvailabilityOverrideTable.teacherId eq teacherId) and
                    (AvailabilityOverrideTable.date eq date)
        }.map { mapRowToOverride(it) }
    }

    fun saveOverride(override: AvailabilityOverride): AvailabilityOverride = transaction {
        val existing = AvailabilityOverrideTable.select {
            (AvailabilityOverrideTable.teacherId eq override.teacherId) and
                    (AvailabilityOverrideTable.date eq override.date) and
                    (AvailabilityOverrideTable.hour eq override.hour)
        }.singleOrNull()

        if (existing == null) {
            AvailabilityOverrideTable.insert {
                it[id] = override.id
                it[teacherId] = override.teacherId
                it[date] = override.date
                it[hour] = override.hour
                it[isAvailable] = override.isAvailable
            }
        } else {
            AvailabilityOverrideTable.update({
                (AvailabilityOverrideTable.teacherId eq override.teacherId) and
                        (AvailabilityOverrideTable.date eq override.date) and
                        (AvailabilityOverrideTable.hour eq override.hour)
            }) {
                it[isAvailable] = override.isAvailable
            }
        }
        override
    }

    fun deleteOverride(teacherId: String, date: String, hour: Double): Boolean = transaction {
        AvailabilityOverrideTable.deleteWhere {
            (AvailabilityOverrideTable.teacherId eq teacherId) and
                    (AvailabilityOverrideTable.date eq date) and
                    (AvailabilityOverrideTable.hour eq hour)
        } > 0
    }

    // ─── Hour Range ─────────────────────────────────────────

    fun getHourRange(teacherId: String): TeacherHourRange? = transaction {
        TeacherHourRangeTable.select {
            TeacherHourRangeTable.teacherId eq teacherId
        }.map { mapRowToHourRange(it) }.singleOrNull()
    }

    fun saveHourRange(hourRange: TeacherHourRange): TeacherHourRange = transaction {
        val exists = TeacherHourRangeTable.select {
            TeacherHourRangeTable.teacherId eq hourRange.teacherId
        }.count() > 0

        if (!exists) {
            TeacherHourRangeTable.insert {
                it[id] = hourRange.id
                it[teacherId] = hourRange.teacherId
                it[startHour] = hourRange.startHour
                it[endHour] = hourRange.endHour
            }
        } else {
            TeacherHourRangeTable.update({
                TeacherHourRangeTable.teacherId eq hourRange.teacherId
            }) {
                it[startHour] = hourRange.startHour
                it[endHour] = hourRange.endHour
            }
        }
        hourRange
    }

    // ─── Mappers ─────────────────────────────────────────────

    // ─── Stamp (bulk, single transaction) ───────────────────

    fun stampOverrides(
        teacherId: String,
        dates: List<String>,
        preservedHoursByDate: Map<String, Set<Double>>,
        newOverrides: List<AvailabilityOverride>
    ) = transaction {
        // Delete existing overrides for each date, keeping any that sit on a live booking
        for (date in dates) {
            val keep = preservedHoursByDate[date] ?: emptySet()
            if (keep.isEmpty()) {
                AvailabilityOverrideTable.deleteWhere {
                    (AvailabilityOverrideTable.teacherId eq teacherId) and
                    (AvailabilityOverrideTable.date eq date)
                }
            } else {
                AvailabilityOverrideTable.deleteWhere {
                    (AvailabilityOverrideTable.teacherId eq teacherId) and
                    (AvailabilityOverrideTable.date eq date) and
                    not(AvailabilityOverrideTable.hour.inList(keep.toList()))
                }
            }
        }
        // Bulk-insert all new overrides in the same transaction
        AvailabilityOverrideTable.batchInsert(newOverrides) { o ->
            this[AvailabilityOverrideTable.id]          = o.id
            this[AvailabilityOverrideTable.teacherId]   = o.teacherId
            this[AvailabilityOverrideTable.date]        = o.date
            this[AvailabilityOverrideTable.hour]        = o.hour
            this[AvailabilityOverrideTable.isAvailable] = o.isAvailable
        }
    }

    // ─── Platonic Slots ──────────────────────────────────────

    fun getPlatonicSlots(teacherId: String): List<PlatonicSlot> = transaction {
        PlatonicSlotTable.select { PlatonicSlotTable.teacherId eq teacherId }
            .map { mapRowToPlatonicSlot(it) }
    }

    fun savePlatonicSlot(slot: PlatonicSlot): PlatonicSlot = transaction {
        val exists = PlatonicSlotTable.select {
            (PlatonicSlotTable.teacherId  eq slot.teacherId)  and
            (PlatonicSlotTable.weekNumber eq slot.weekNumber) and
            (PlatonicSlotTable.dayOfWeek  eq slot.dayOfWeek)  and
            (PlatonicSlotTable.hour       eq slot.hour)
        }.count() > 0

        if (!exists) {
            PlatonicSlotTable.insert {
                it[id]         = slot.id
                it[teacherId]  = slot.teacherId
                it[weekNumber] = slot.weekNumber
                it[dayOfWeek]  = slot.dayOfWeek
                it[hour]       = slot.hour
            }
        }
        slot
    }

    fun deletePlatonicSlot(
        teacherId: String,
        weekNumber: Int,
        dayOfWeek: Int,
        hour: Double
    ): Boolean = transaction {
        PlatonicSlotTable.deleteWhere {
            (PlatonicSlotTable.teacherId  eq teacherId)  and
            (PlatonicSlotTable.weekNumber eq weekNumber) and
            (PlatonicSlotTable.dayOfWeek  eq dayOfWeek)  and
            (PlatonicSlotTable.hour       eq hour)
        } > 0
    }

    // ─── Mappers ─────────────────────────────────────────────

    private fun mapRowToPlatonicSlot(row: ResultRow): PlatonicSlot = PlatonicSlot(
        id         = row[PlatonicSlotTable.id].value,
        teacherId  = row[PlatonicSlotTable.teacherId],
        weekNumber = row[PlatonicSlotTable.weekNumber],
        dayOfWeek  = row[PlatonicSlotTable.dayOfWeek],
        hour       = row[PlatonicSlotTable.hour]
    )

    private fun mapRowToWeeklySlot(row: ResultRow): WeeklySlot {
        return WeeklySlot(
            id = row[WeeklySlotTable.id].value,
            teacherId = row[WeeklySlotTable.teacherId],
            dayOfWeek = row[WeeklySlotTable.dayOfWeek],
            hour = row[WeeklySlotTable.hour]
        )
    }

    private fun mapRowToOverride(row: ResultRow): AvailabilityOverride {
        return AvailabilityOverride(
            id = row[AvailabilityOverrideTable.id].value,
            teacherId = row[AvailabilityOverrideTable.teacherId],
            date = row[AvailabilityOverrideTable.date],
            hour = row[AvailabilityOverrideTable.hour],
            isAvailable = row[AvailabilityOverrideTable.isAvailable]
        )
    }

    private fun mapRowToHourRange(row: ResultRow): TeacherHourRange {
        return TeacherHourRange(
            id = row[TeacherHourRangeTable.id].value,
            teacherId = row[TeacherHourRangeTable.teacherId],
            startHour = row[TeacherHourRangeTable.startHour],
            endHour = row[TeacherHourRangeTable.endHour]
        )
    }
}