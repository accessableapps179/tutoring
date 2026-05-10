package com.marketplace.repository

import com.marketplace.domain.Teacher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TeacherRepositoryTest {

    private val teacherRepository = TeacherRepository()

    @Test
    fun testSave() {
        val teacher = Teacher(
            id = "1",
            name = "John Doe",
            hourlyRate = 30.0,
            aboutMe = "Experienced teacher"
        )

        val savedTeacher = teacherRepository.save(teacher)

        // Print repository content
        println("After save, repository contains: ${teacherRepository.findAll()}")

        assertEquals(teacher, savedTeacher)
    }

    @Test
    fun testFindAll() {
        val teacher1 = Teacher(
            id = "1",
            name = "John Doe",
            hourlyRate = 30.0,
            aboutMe = "Experienced teacher"
        )
        val teacher2 = Teacher(
            id = "2",
            name = "Jane Smith",
            hourlyRate = 40.0,
            aboutMe = "Math teacher"
        )

        teacherRepository.save(teacher1)
        teacherRepository.save(teacher2)

        val teachers = teacherRepository.findAll()
        println("All teachers in repository: $teachers")

        assertEquals(2, teachers.size)
    }

    @Test
    fun testFindById() {
        val teacher = Teacher(
            id = "1",
            name = "John Doe",
            hourlyRate = 30.0,
            aboutMe = "Experienced teacher"
        )

        teacherRepository.save(teacher)

        val foundTeacher = teacherRepository.findById("1")
        println("Teacher found by ID 1: $foundTeacher")
        assertEquals(teacher, foundTeacher)

        val notFoundTeacher = teacherRepository.findById("2")
        println("Teacher found by ID 2 (should be null): $notFoundTeacher")
        assertNull(notFoundTeacher)
    }

    @Test
    fun testDelete() {
        val teacher = Teacher(
            id = "1",
            name = "John Doe",
            hourlyRate = 30.0,
            aboutMe = "Experienced teacher"
        )

        teacherRepository.save(teacher)
        println("Repository before delete: ${teacherRepository.findAll()}")

        val isDeleted = teacherRepository.delete("1")
        println("Deleted teacher with ID 1: $isDeleted")
        println("Repository after delete: ${teacherRepository.findAll()}")
        assertEquals(true, isDeleted)

        val teacherAfterDelete = teacherRepository.findById("1")
        assertNull(teacherAfterDelete)

        val isDeletedAgain = teacherRepository.delete("1")
        println("Attempt to delete teacher ID 1 again: $isDeletedAgain")
        assertEquals(false, isDeletedAgain)
    }
}