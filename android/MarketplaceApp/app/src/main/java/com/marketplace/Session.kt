// app/src/main/java/com/marketplace/Session.kt
package com.marketplace

import com.marketplace.dto.TeacherDto
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Lightweight in-memory session store.
 * Populated on login/register, cleared on logout.
 *
 * Also used to pass complex navigation state (names, teacher data) that
 * cannot safely travel through URL path segments due to special characters.
 */
object Session {

    // ─── Auth ─────────────────────────────────────────────────────────────────

    var token: String = ""
    var role: String  = ""
    var userId: String = ""
    var name: String  = ""

    // ─── Search state ─────────────────────────────────────────────────────────

    var lastSearchTargetLanguage: String = ""
    var lastSearchInstructionLanguage: String? = null
    var lastSearchResults: List<TeacherDto> = emptyList()

    // ─── Navigation state ─────────────────────────────────────────────────────

    /** Set before navigating to teacher_detail. */
    var selectedTeacher: TeacherDto? = null

    /**
     * Set before navigating to any lobby/call/rejoin screen.
     * Holds the display name of the other person in the call.
     */
    var pendingCallName: String = ""

    /**
     * Set before navigating to book_teacher.
     * Holds the teacher name and student name for SlotBookingScreen.
     */
    var pendingTeacherName: String = ""

    /**
     * Set after a happy trial result. Holds the contact ID so that after
     * booking the 2nd lesson the student is taken directly to the chat screen.
     * Cleared once used or on logout.
     */
    var pendingContactId: String = ""

    /** Set from MonthCalendarScreen when the student picks a date before booking. */
    var pendingBookingDate: LocalDate? = null

    // ─── Debug / Time override ─────────────────────────────────────────────────

    /** Set from the main screen to simulate a different date/time during testing. */
    var debugDateTime: LocalDateTime? = null

    fun currentDateTime(): LocalDateTime = debugDateTime ?: LocalDateTime.now()

    // ─── Helpers ──────────────────────────────────────────────────────────────

    fun populate(token: String, role: String, userId: String, name: String) {
        this.token  = token
        this.role   = role
        this.userId = userId
        this.name   = name
    }

    fun clear() {
        token   = ""
        role    = ""
        userId  = ""
        name    = ""
        lastSearchTargetLanguage      = ""
        lastSearchInstructionLanguage = null
        lastSearchResults             = emptyList()
        selectedTeacher               = null
        pendingCallName               = ""
        pendingTeacherName            = ""
        pendingContactId              = ""
        pendingBookingDate            = null
        debugDateTime                 = null
    }
}