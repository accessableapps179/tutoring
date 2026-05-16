// app/src/main/java/com/marketplace/ui/navigation/AppNavGraph.kt
package com.marketplace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marketplace.Session
import com.marketplace.ui.screens.AdminScreen
import com.marketplace.ui.screens.BankDetailsScreen
import com.marketplace.ui.screens.BookingSuccessScreen
import com.marketplace.ui.screens.ChangePasswordScreen
import com.marketplace.ui.screens.ChatScreen
import com.marketplace.ui.screens.DebugScreen
import com.marketplace.ui.screens.LobbyScreen
import com.marketplace.ui.screens.LoginScreen
import com.marketplace.ui.screens.LogoutScreen
import com.marketplace.ui.screens.MessagesListScreen
import com.marketplace.ui.screens.MonthCalendarScreen
import com.marketplace.ui.screens.MyAccountScreen
import com.marketplace.ui.screens.MyBookingsScreen
import com.marketplace.ui.screens.NotHappyFunnelScreen
import com.marketplace.ui.screens.PostTrialScreen
import com.marketplace.ui.screens.PaymentCardScreen
import com.marketplace.ui.screens.RegisterScreen
import com.marketplace.ui.screens.RejoinScreen
import com.marketplace.ui.screens.SlotBookingScreen
import com.marketplace.ui.screens.StudentBookingScreen
import com.marketplace.ui.screens.StudentLedgerScreen
import com.marketplace.ui.screens.PlatonicAvailabilityScreen
import com.marketplace.ui.screens.TeacherAvailabilityScreen
import com.marketplace.ui.screens.TeacherBalanceScreen
import com.marketplace.ui.screens.TeacherDetailScreen
import com.marketplace.ui.screens.TeacherListScreen
import com.marketplace.ui.screens.TeacherProfileScreen
import com.marketplace.ui.screens.TrialResultScreen
import com.marketplace.ui.screens.VideoCallScreen
import com.marketplace.viewmodel.AuthViewModel
import com.marketplace.viewmodel.MessageViewModel

@Composable
fun AppNavGraph() {
    val navController    = rememberNavController()
    val authViewModel    : AuthViewModel    = viewModel()
    val messageViewModel : MessageViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        // ─── Auth ──────────────────────────────────────────────────────────────

        composable("login") {
            LoginScreen(
                onLoginSuccess = { token, role, userId, name ->
                    Session.populate(token, role, userId, name)
                    navController.navigateHome()
                },
                onRegisterClick  = { navController.navigate("register") },
                onDebugClick     = { navController.navigate("debug") },
                onAdminClick     = { navController.navigate("admin") },
                messageViewModel = messageViewModel
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    val state = authViewModel.authState.value
                    Session.populate(state.token, state.role, state.userId, state.name)
                    navController.navigateHome()
                },
                onBackToLogin = rememberSingleClick { navController.popBackStack() },
                viewModel     = authViewModel
            )
        }

        composable("logout") {
            LogoutScreen(
                authViewModel    = authViewModel,
                messageViewModel = messageViewModel,
                onLogoutComplete = {
                    Session.clear()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onCancelLogout = { navController.popBackStack() }
            )
        }

        composable("change_password") {
            ChangePasswordScreen(onBackClick = rememberSingleClick { navController.popBackStack() })
        }

        // ─── Utility / Dev ─────────────────────────────────────────────────────

        composable("debug") {
            DebugScreen(
                onBackClick    = rememberSingleClick { navController.popBackStack() },
                onNukeComplete = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("admin") {
            AdminScreen(onBackClick = rememberSingleClick { navController.popBackStack() })
        }

        // ─── Home / Teacher List ───────────────────────────────────────────────
        //
        // Token + identity still travel as path args so navigateHome() can
        // reconstruct the route and the back stack can restore this screen.
        // They are safe in URLs because they are UUIDs / JWT strings with no
        // special characters.

        composable(
            route = "teacher_list/{token}/{role}/{userId}/{name}",
            arguments = listOf(
                navArgument("token")  { type = NavType.StringType },
                navArgument("role")   { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("name")   { type = NavType.StringType }
            )
        ) { back ->
            val token  = back.arguments?.getString("token")  ?: ""
            val role   = back.arguments?.getString("role")   ?: ""
            val userId = back.arguments?.getString("userId") ?: ""
            val name   = back.arguments?.getString("name")   ?: ""
            if (token.isNotEmpty()) Session.populate(token, role, userId, name)

            TeacherListScreen(
                role     = role,
                userName = name,
                onTeacherClick = { teacher ->
                    // Store the full teacher object — never put free-text in the URL
                    Session.selectedTeacher = teacher
                    navController.navigate("teacher_detail/${teacher.id}")
                },
                onMyBookingsClick = {
                    if (role == "STUDENT") navController.navigate("my_lessons/$userId")
                    else navController.navigate("my_bookings/$role")
                },
                onMyTutorClick = { teacherId, teacherName, contactId ->
                    Session.pendingTeacherName = teacherName
                    Session.pendingContactId = contactId
                    navController.navigate("month_calendar/$teacherId")
                },
                onManageProfileClick      = { navController.navigate("teacher_profile/$userId") },
                onManageAvailabilityClick = { navController.navigate("teacher_availability/$userId") },
                onMessagesClick  = { navController.navigate("messages_list/$role/$userId") },
                onMyAccountClick = { navController.navigate("my_account/$role") },
                onLogoutClick    = { navController.navigate("logout") },
                messageViewModel      = messageViewModel
            )
        }

        // ─── Teacher ───────────────────────────────────────────────────────────

        composable(
            route = "teacher_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { back ->
            val teacherId = back.arguments?.getString("id") ?: ""
            val teacher   = Session.selectedTeacher

            // Guard: Session lost (process death) — go back rather than crash
            if (teacher == null || teacher.id != teacherId) {
                navController.popBackStack()
                return@composable
            }

            TeacherDetailScreen(
                teacherId            = teacher.id,
                teacherName          = teacher.name,
                teacherHourlyRate    = teacher.hourlyRate,
                teacherAboutMe       = teacher.aboutMe,
                teachingLanguages    = teacher.teachingLanguages,
                instructionLanguages = teacher.instructionLanguages,
                role                 = Session.role,
                userId               = Session.userId,
                onBackClick          = rememberSingleClick { navController.popBackStack() },
                onBookClick          = if (Session.role == "STUDENT") {
                    {
                        Session.pendingTeacherName = teacher.name
                        Session.pendingContactId = ""
                        navController.navigate("month_calendar/${teacher.id}")
                    }
                } else null,
                onMessageClick = { contactId ->
                    // otherPersonName stored in Session, only ID in URL
                    Session.pendingCallName = teacher.name
                    navController.navigate("chat/$contactId/${Session.userId}")
                }
            )
        }

        // Teacher profile only needs userId (UUID, safe in URL).
        // userName is read from Session.name.
        composable(
            route = "teacher_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { back ->
            TeacherProfileScreen(
                userId      = back.arguments?.getString("userId") ?: "",
                userName    = Session.name,
                onBackClick = rememberSingleClick { navController.popBackStack() }
            )
        }

        composable(
            route = "teacher_availability/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            TeacherAvailabilityScreen(
                onBackClick         = rememberSingleClick { navController.popBackStack() },
                onEditTemplateClick = { navController.navigate("platonic_availability") },
                onStartVideoCall    = { contactId, otherPersonName ->
                    Session.pendingCallName = otherPersonName
                    navController.navigate("lobby/$contactId")
                },
                onRejoinCall = { contactId, otherPersonName ->
                    Session.pendingCallName = otherPersonName
                    navController.navigate("video_call/$contactId")
                }
            )
        }

        composable("platonic_availability") {
            PlatonicAvailabilityScreen(
                onBackClick      = rememberSingleClick { navController.popBackStack() },
                onStampComplete  = { navController.popBackStack() }
            )
        }

        // ─── Booking ───────────────────────────────────────────────────────────

        composable(
            route = "month_calendar/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { back ->
            val teacherId = back.arguments?.getString("teacherId") ?: ""
            if (Session.pendingTeacherName.isEmpty()) {
                navController.popBackStack()
                return@composable
            }
            MonthCalendarScreen(
                teacherName    = Session.pendingTeacherName,
                onBackClick    = { navController.popBackStack() },
                onDateSelected = { date ->
                    Session.pendingBookingDate = date
                    navController.navigate("book_teacher/$teacherId")
                }
            )
        }

        // Only teacherId (UUID) in URL. Teacher name and student name come from Session.
        composable(
            route = "book_teacher/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { back ->
            val teacherId = back.arguments?.getString("teacherId") ?: ""

            // Guard: if Session lost, go back
            if (Session.pendingTeacherName.isEmpty()) {
                navController.popBackStack()
                return@composable
            }

            SlotBookingScreen(
                teacherId   = teacherId,
                teacherName = Session.pendingTeacherName,
                studentName = Session.name,
                onBackClick = rememberSingleClick { navController.popBackStack() },
                onCalendarClick = {
                    navController.navigate("month_calendar/$teacherId") {
                        popUpTo("month_calendar/$teacherId") { inclusive = true }
                    }
                },
                onBookingSuccess = {
                    Session.pendingContactId = ""
                    navController.navigate("booking_success") {
                        popUpTo("book_teacher/$teacherId") { inclusive = true }
                    }
                }
            )
        }

        composable("booking_success") {
            BookingSuccessScreen(onDone = rememberSingleClick { navController.navigateHome() })
        }

        composable(
            route = "my_lessons/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            StudentBookingScreen(
                onBackClick      = rememberSingleClick { navController.popBackStack() },
                onStartVideoCall = { contactId, otherPersonName, bookingId, teacherId ->
                    Session.pendingCallName = otherPersonName
                    navController.navigate("lobby_trial/$contactId/$bookingId/$teacherId")
                },
                onStartRegularCall = { contactId, otherPersonName ->
                    Session.pendingCallName = otherPersonName
                    navController.navigate("lobby/$contactId")
                }
            )
        }

        composable(
            route = "my_bookings/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { back ->
            val role = back.arguments?.getString("role") ?: ""
            MyBookingsScreen(
                role        = role,
                onBackClick = rememberSingleClick { navController.popBackStack() },
                onStartVideoCall = { contactId, otherPersonName ->
                    Session.pendingCallName = otherPersonName
                    navController.navigate("lobby/$contactId")
                }
            )
        }

        // ─── Messaging ─────────────────────────────────────────────────────────

        composable(
            route = "messages_list/{role}/{userId}",
            arguments = listOf(
                navArgument("role")   { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { back ->
            val role   = back.arguments?.getString("role")   ?: ""
            val userId = back.arguments?.getString("userId") ?: ""
            MessagesListScreen(
                userId           = userId,
                role             = role,
                onBackClick      = rememberSingleClick { navController.popBackStack() },
                onChatClick      = { contactId, otherPersonName ->
                    Session.pendingCallName = otherPersonName
                    navController.navigate("chat/$contactId/$userId")
                },
                messageViewModel = messageViewModel
            )
        }

        // Only IDs in URL. otherPersonName read from Session.pendingCallName.
        composable(
            route = "chat/{contactId}/{userId}",
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType },
                navArgument("userId")    { type = NavType.StringType }
            )
        ) { back ->
            val contactId = back.arguments?.getString("contactId") ?: ""
            val userId    = back.arguments?.getString("userId")    ?: ""
            ChatScreen(
                contactId         = contactId,
                otherPersonName   = Session.pendingCallName,
                currentUserId     = userId,
                onBackClick       = rememberSingleClick { navController.popBackStack() },
                onVideoCallClick  = { navController.navigate("lobby/$contactId") },
                onVideoCallRejoin = { navController.navigate("video_call/$contactId") }
            )
        }

        // ─── Video Call ────────────────────────────────────────────────────────
        //
        // All call routes carry only contactId (UUID).
        // otherPersonName is read from Session.pendingCallName, which is set
        // by every caller before navigating.

        composable(
            route = "lobby/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { back ->
            val contactId = back.arguments?.getString("contactId") ?: ""
            LobbyScreen(
                contactId       = contactId,
                otherPersonName = Session.pendingCallName,
                onBackClick     = rememberSingleClick { navController.popBackStack() },
                onCallReady     = {
                    navController.navigate("video_call/$contactId") {
                        popUpTo("lobby/$contactId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "lobby_trial/{contactId}/{bookingId}/{teacherId}",
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType },
                navArgument("bookingId") { type = NavType.StringType },
                navArgument("teacherId") { type = NavType.StringType }
            )
        ) { back ->
            val contactId = back.arguments?.getString("contactId") ?: ""
            val bookingId = back.arguments?.getString("bookingId") ?: ""
            val teacherId = back.arguments?.getString("teacherId") ?: ""
            LobbyScreen(
                contactId       = contactId,
                otherPersonName = Session.pendingCallName,
                onBackClick     = rememberSingleClick { navController.popBackStack() },
                onCallReady     = {
                    navController.navigate("video_call_trial/$contactId/$bookingId/$teacherId") {
                        popUpTo("lobby_trial/$contactId/$bookingId/$teacherId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "video_call/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { back ->
            val contactId = back.arguments?.getString("contactId") ?: ""
            VideoCallScreen(
                contactId       = contactId,
                otherPersonName = Session.pendingCallName,
                role            = "",
                onEndCall       = rememberSingleClick {
                    navController.navigate("rejoin/$contactId") {
                        popUpTo("video_call/$contactId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "video_call_trial/{contactId}/{bookingId}/{teacherId}",
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType },
                navArgument("bookingId") { type = NavType.StringType },
                navArgument("teacherId") { type = NavType.StringType }
            )
        ) { back ->
            val contactId = back.arguments?.getString("contactId") ?: ""
            val bookingId = back.arguments?.getString("bookingId") ?: ""
            val teacherId = back.arguments?.getString("teacherId") ?: ""
            VideoCallScreen(
                contactId       = contactId,
                otherPersonName = Session.pendingCallName,
                role            = "STUDENT",
                onEndCall       = rememberSingleClick {
                    navController.navigate("trial_result/$bookingId/$teacherId") {
                        popUpTo("video_call_trial/$contactId/$bookingId/$teacherId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "rejoin/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { back ->
            val contactId = back.arguments?.getString("contactId") ?: ""
            RejoinScreen(
                otherPersonName = Session.pendingCallName,
                onRejoin = {
                    navController.navigate("video_call/$contactId") {
                        popUpTo("rejoin/$contactId") { inclusive = true }
                    }
                },
                onLeave = { navController.popBackStack() }
            )
        }

        // ─── Trial / Funnel ────────────────────────────────────────────────────
        //
        // teacherName removed from URL — read from Session.selectedTeacher.
        // bookingId and teacherId are UUIDs, safe in URLs.

        composable(
            route = "trial_result/{bookingId}/{teacherId}",
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType },
                navArgument("teacherId") { type = NavType.StringType }
            )
        ) { back ->
            val bookingId   = back.arguments?.getString("bookingId") ?: ""
            val teacherId   = back.arguments?.getString("teacherId") ?: ""
            val teacherName = Session.selectedTeacher?.name ?: Session.pendingCallName

            TrialResultScreen(
                bookingId              = bookingId,
                teacherId              = teacherId,
                teacherName            = teacherName,
                onHappyContactUnlocked = {
                    Session.pendingTeacherName = teacherName
                    navController.navigate("post_trial/$teacherId")
                },
                onNotHappy             = {
                    Session.lastSearchResults = Session.lastSearchResults.filter { it.id != teacherId }
                    navController.navigate("not_happy_funnel") {
                        popUpTo("login") { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = "post_trial/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { back ->
            val teacherId = back.arguments?.getString("teacherId") ?: ""
            PostTrialScreen(
                teacherName = Session.pendingTeacherName,
                onBookNow   = { navController.navigate("month_calendar/$teacherId") },
                onBookLater = { navController.navigateHome() }
            )
        }

        composable("not_happy_funnel") {
            NotHappyFunnelScreen(
                initialTargetLanguage      = Session.lastSearchTargetLanguage,
                initialInstructionLanguage = Session.lastSearchInstructionLanguage,
                initialResults             = Session.lastSearchResults,
                onTeacherClick = { teacher ->
                    Session.selectedTeacher = teacher
                    navController.navigate("teacher_detail/${teacher.id}") {
                        popUpTo("not_happy_funnel") { inclusive = true }
                    }
                },
                onHomeClick = { navController.navigateHome() }
            )
        }

        // ─── Account / Finance ─────────────────────────────────────────────────

        composable(
            route = "my_account/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { back ->
            val role = back.arguments?.getString("role") ?: ""
            MyAccountScreen(
                role                  = role,
                onBackClick           = rememberSingleClick { navController.popBackStack() },
                onChangePasswordClick = { navController.navigate("change_password") },
                onPaymentCardClick    = { navController.navigate("payment_card") },
                onBankDetailsClick    = { navController.navigate("bank_details") },
                onMyBalanceClick      = {
                    if (role == "TEACHER") navController.navigate("teacher_balance")
                    else navController.navigate("student_ledger")
                }
            )
        }

        composable("bank_details") {
            BankDetailsScreen(onBackClick = rememberSingleClick { navController.popBackStack() })
        }

        composable("payment_card") {
            PaymentCardScreen(onBackClick = rememberSingleClick { navController.popBackStack() })
        }

        composable("teacher_balance") {
            TeacherBalanceScreen(onBackClick = rememberSingleClick { navController.popBackStack() })
        }

        composable("student_ledger") {
            StudentLedgerScreen(onBackClick = rememberSingleClick { navController.popBackStack() })
        }
    }
}