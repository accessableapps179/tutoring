// MODIFIED FILE
// src/main/kotlin/com/marketplace/service/TeacherService.kt
package com.marketplace.service

import com.marketplace.domain.AppLanguage
import com.marketplace.domain.Teacher
import com.marketplace.repository.TeacherRepository

class TeacherService(private val repository: TeacherRepository = TeacherRepository()) {

    fun getAll(): List<Teacher> {
        return repository.findAll()
    }

    fun getById(id: String): Teacher? {
        return repository.findById(id)
    }

    // Search listed teachers by target language (required) and instruction language (optional).
    // Both values must be valid AppLanguage codes.
    fun search(targetLanguage: String, instructionLanguage: String?): List<Teacher> {
        require(AppLanguage.fromCode(targetLanguage) != null) {
            "Invalid target language code: $targetLanguage. Valid codes: ${AppLanguage.allCodes}"
        }
        if (instructionLanguage != null) {
            require(AppLanguage.fromCode(instructionLanguage) != null) {
                "Invalid instruction language code: $instructionLanguage. Valid codes: ${AppLanguage.allCodes}"
            }
        }
        return repository.search(targetLanguage, instructionLanguage)
    }

    fun save(teacher: Teacher): Teacher {
        validateTeacher(teacher)
        return repository.save(teacher)
    }

    fun create(teacher: Teacher): Teacher {
        validateTeacher(teacher)
        return repository.save(teacher)
    }

    fun update(id: String, updatedTeacher: Teacher): Teacher? {
        repository.findById(id) ?: return null
        validateTeacher(updatedTeacher)
        val teacherToSave = updatedTeacher.copy(id = id)
        return repository.save(teacherToSave)
    }

    fun delete(id: String): Boolean {
        return repository.delete(id)
    }

    private fun validateTeacher(teacher: Teacher) {
        require(teacher.aboutMe.length in 20..4000) {
            "aboutMe must be between 20 and 4000 characters"
        }
        require(teacher.hourlyRate >= 0) {
            "hourlyRate must be >= 0"
        }
        // Validate that all supplied language codes are known
        teacher.teachingLanguages.forEach { code ->
            require(AppLanguage.fromCode(code) != null) {
                "Invalid teaching language code: $code"
            }
        }
        teacher.instructionLanguages.forEach { code ->
            require(AppLanguage.fromCode(code) != null) {
                "Invalid instruction language code: $code"
            }
        }
    }
}
