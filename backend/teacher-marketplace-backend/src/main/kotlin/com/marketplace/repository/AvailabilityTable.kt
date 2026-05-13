package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object WeeklySlotTable : IdTable<String>("weekly_slots") {
    override val id = varchar("id", 50).entityId()
    val teacherId = varchar("teacher_id", 50)
    val dayOfWeek = integer("day_of_week")
    val hour = double("hour")
}

object AvailabilityOverrideTable : IdTable<String>("availability_overrides") {
    override val id = varchar("id", 50).entityId()
    val teacherId = varchar("teacher_id", 50)
    val date = varchar("date", 20)
    val hour = double("hour")
    val isAvailable = bool("is_available")
}

object TeacherHourRangeTable : IdTable<String>("teacher_hour_ranges") {
    override val id = varchar("id", 50).entityId()
    val teacherId = varchar("teacher_id", 50).uniqueIndex()
    val startHour = integer("start_hour").default(6)
    val endHour = integer("end_hour").default(22)
}

object PlatonicSlotTable : IdTable<String>("platonic_slots") {
    override val id = varchar("id", 50).entityId()
    val teacherId  = varchar("teacher_id", 50)
    val weekNumber = integer("week_number")   // 1–4
    val dayOfWeek  = integer("day_of_week")   // 1=Mon … 7=Sun
    val hour       = double("hour")
}