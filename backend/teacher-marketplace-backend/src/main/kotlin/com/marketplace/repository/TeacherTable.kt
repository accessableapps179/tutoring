// MODIFIED FILE
// src/main/kotlin/com/marketplace/repository/TeacherTable.kt
package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object TeacherTable : IdTable<String>("teacher") {
    override val id = varchar("id", 50).entityId()
    val name: Column<String> = varchar("name", 255)
    val hourlyRate: Column<Double> = double("hourlyRate")
    val aboutMe: Column<String> = text("aboutMe")
    val isListed: Column<Boolean> = bool("isListed").default(false)
    // Comma-separated language codes, e.g. "en,vi" — empty string = none set
    val teachingLanguages: Column<String> = text("teaching_languages").default("")
    val instructionLanguages: Column<String> = text("instruction_languages").default("")
}
