package com.marketplace.service

import com.marketplace.domain.Contact
import com.marketplace.repository.ContactRepository
import java.util.UUID

class ContactService(
    private val contactRepository: ContactRepository = ContactRepository()
) {

    fun sendContactRequest(
        studentId: String,
        teacherId: String,
        studentName: String,
        teacherName: String
    ): Contact {
        val existing = contactRepository.findByStudentAndTeacher(studentId, teacherId)

        if (existing != null && existing.status in listOf("PENDING", "ACCEPTED")) {
            return existing
        }

        if (existing != null && existing.status == "DECLINED") {
            contactRepository.updateStatus(existing.id, "PENDING")
            return existing.copy(status = "PENDING")
        }

        val contact = Contact(
            id = UUID.randomUUID().toString(),
            studentId = studentId,
            teacherId = teacherId,
            studentName = studentName,
            teacherName = teacherName,
            status = "PENDING"
        )
        return contactRepository.save(contact)
    }

    // Called after student says happy — directly creates an ACCEPTED contact
    fun acceptContactByStudentAndTeacher(studentId: String, teacherId: String): Boolean {
        val existing = contactRepository.findByStudentAndTeacher(studentId, teacherId)
        return if (existing != null) {
            contactRepository.updateStatus(existing.id, "ACCEPTED")
        } else {
            false
        }
    }

    fun getContactsForUser(userId: String): List<Contact> {
        return contactRepository.findByUserId(userId)
            .filter { it.status == "ACCEPTED" }
    }

    fun getPendingRequestsForTeacher(teacherId: String): List<Contact> {
        return contactRepository.findPendingForTeacher(teacherId)
    }

    fun acceptContact(contactId: String): Boolean {
        return contactRepository.updateStatus(contactId, "ACCEPTED")
    }

    fun declineContact(contactId: String): Boolean {
        return contactRepository.updateStatus(contactId, "DECLINED")
    }

    fun getContactStatus(studentId: String, teacherId: String): String? {
        return contactRepository.findByStudentAndTeacher(studentId, teacherId)?.status
    }

    fun getContactById(contactId: String): Contact? {
        return contactRepository.findById(contactId)
    }

    fun getContactBetween(studentId: String, teacherId: String): Contact? {
        return contactRepository.findByStudentAndTeacher(studentId, teacherId)
    }
}