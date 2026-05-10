package com.marketplace.repository

import com.marketplace.domain.Contact
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ContactRepository {

    fun save(contact: Contact): Contact = transaction {
        ContactTable.insert {
            it[id] = contact.id
            it[studentId] = contact.studentId
            it[teacherId] = contact.teacherId
            it[studentName] = contact.studentName
            it[teacherName] = contact.teacherName
            it[status] = contact.status
        }
        contact
    }

    fun findByStudentAndTeacher(studentId: String, teacherId: String): Contact? = transaction {
        ContactTable.select {
            (ContactTable.studentId eq studentId) and
                    (ContactTable.teacherId eq teacherId)
        }.map { mapRowToContact(it) }.singleOrNull()
    }

    fun findByUserId(userId: String): List<Contact> = transaction {
        ContactTable.select {
            (ContactTable.studentId eq userId) or
                    (ContactTable.teacherId eq userId)
        }.map { mapRowToContact(it) }
    }

    fun findPendingForTeacher(teacherId: String): List<Contact> = transaction {
        ContactTable.select {
            (ContactTable.teacherId eq teacherId) and
                    (ContactTable.status eq "PENDING")
        }.map { mapRowToContact(it) }
    }

    fun updateStatus(id: String, status: String): Boolean = transaction {
        ContactTable.update({ ContactTable.id eq id }) {
            it[ContactTable.status] = status
        } > 0
    }

    fun findById(id: String): Contact? = transaction {
        ContactTable.select { ContactTable.id eq id }
            .map { mapRowToContact(it) }
            .singleOrNull()
    }

    private fun mapRowToContact(row: ResultRow): Contact {
        return Contact(
            id = row[ContactTable.id].value,
            studentId = row[ContactTable.studentId],
            teacherId = row[ContactTable.teacherId],
            studentName = row[ContactTable.studentName],
            teacherName = row[ContactTable.teacherName],
            status = row[ContactTable.status]
        )
    }
}