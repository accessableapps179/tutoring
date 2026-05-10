package com.marketplace.repository
import org.jetbrains.exposed.sql.and
import com.marketplace.domain.Message
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.or

class MessageRepository {

    fun save(message: Message): Message = transaction {
        MessageTable.insert {
            it[id] = message.id
            it[contactId] = message.contactId
            it[senderId] = message.senderId
            it[senderName] = message.senderName
            it[content] = message.content
            it[timestamp] = message.timestamp
            it[isRead] = message.isRead
        }
        message
    }

    fun findByContactId(contactId: String): List<Message> = transaction {
        MessageTable.select { MessageTable.contactId eq contactId }
            .orderBy(MessageTable.timestamp)
            .map { mapRowToMessage(it) }
    }

    fun markAsRead(contactId: String, userId: String): Int = transaction {
        MessageTable.update({
            (MessageTable.contactId eq contactId) and
                    (MessageTable.senderId neq userId)
        }) {
            it[isRead] = true
        }
    }

    fun countUnread(userId: String, contactId: String): Long = transaction {
        MessageTable.select {
            (MessageTable.contactId eq contactId) and
                    (MessageTable.senderId neq userId) and
                    (MessageTable.isRead eq false)
        }.count()
    }

    fun countAllUnread(userId: String): Long = transaction {
        // Get all contact IDs that belong to this user
        val userContactIds = ContactTable
            .select {
                (ContactTable.studentId eq userId) or
                        (ContactTable.teacherId eq userId)
            }
            .map { it[ContactTable.id].value }

        if (userContactIds.isEmpty()) return@transaction 0L

        MessageTable.select {
            (MessageTable.contactId inList userContactIds) and
                    (MessageTable.senderId neq userId) and
                    (MessageTable.isRead eq false)
        }.count()
    }

    private fun mapRowToMessage(row: ResultRow): Message {
        return Message(
            id = row[MessageTable.id].value,
            contactId = row[MessageTable.contactId],
            senderId = row[MessageTable.senderId],
            senderName = row[MessageTable.senderName],
            content = row[MessageTable.content],
            timestamp = row[MessageTable.timestamp],
            isRead = row[MessageTable.isRead]
        )
    }
}