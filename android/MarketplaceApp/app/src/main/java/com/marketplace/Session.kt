// app/src/main/java/com/marketplace/Session.kt
package com.marketplace

import com.marketplace.dto.TeacherDto

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
    }
}