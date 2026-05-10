// MODIFIED FILE
// src/main/kotlin/com/marketplace/repository/TeacherRepository.kt
package com.marketplace.repository

import com.marketplace.domain.Teacher
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class TeacherRepository {

    // Only returns LISTED teachers — for students to see
    fun findAll(): List<Teacher> = transaction {
        TeacherTable.select { TeacherTable.isListed eq true }
            .map { mapRowToTeacher(it) }
    }

    // Search listed teachers by target language and optional instruction language.
    // targetLanguage must appear in teachingLanguages.
    // If instructionLanguage is provided it must appear in instructionLanguages.
    fun search(targetLanguage: String, instructionLanguage: String?): List<Teacher> = transaction {
        TeacherTable.select { TeacherTable.isListed eq true }
            .map { mapRowToTeacher(it) }
            .filter { teacher ->
                val teachesTarget = teacher.teachingLanguages.contains(targetLanguage)
                val instructionMatch = if (instructionLanguage != null) {
                    teacher.instructionLanguages.contains(instructionLanguage)
                } else {
                    true
                }
                teachesTarget && instructionMatch
            }
    }

    fun findById(id: String): Teacher? = transaction {
        TeacherTable.select { TeacherTable.id eq id }
            .map { mapRowToTeacher(it) }
            .singleOrNull()
    }

    fun save(teacher: Teacher): Teacher = transaction {
        val teachingStr = teacher.teachingLanguages.joinToString(",")
        val instructionStr = teacher.instructionLanguages.joinToString(",")

        val exists = TeacherTable.select { TeacherTable.id eq teacher.id }.count() > 0
        if (!exists) {
            TeacherTable.insert {
                it[id] = teacher.id
                it[name] = teacher.name
                it[hourlyRate] = teacher.hourlyRate
                it[aboutMe] = teacher.aboutMe
                it[isListed] = teacher.isListed
                it[teachingLanguages] = teachingStr
                it[instructionLanguages] = instructionStr
            }
        } else {
            TeacherTable.update({ TeacherTable.id eq teacher.id }) {
                it[name] = teacher.name
                it[hourlyRate] = teacher.hourlyRate
                it[aboutMe] = teacher.aboutMe
                it[isListed] = teacher.isListed
                it[teachingLanguages] = teachingStr
                it[instructionLanguages] = instructionStr
            }
        }
        teacher
    }

    fun delete(id: String): Boolean = transaction {
        TeacherTable.deleteWhere { TeacherTable.id eq id } > 0
    }

    private fun mapRowToTeacher(row: ResultRow): Teacher {
        val teachingStr = row[TeacherTable.teachingLanguages]
        val instructionStr = row[TeacherTable.instructionLanguages]
        return Teacher(
            id = row[TeacherTable.id].value,
            name = row[TeacherTable.name],
            hourlyRate = row[TeacherTable.hourlyRate],
            aboutMe = row[TeacherTable.aboutMe],
            isListed = row[TeacherTable.isListed],
            teachingLanguages = if (teachingStr.isBlank()) emptyList()
                                else teachingStr.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            instructionLanguages = if (instructionStr.isBlank()) emptyList()
                                   else instructionStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )
    }
}
