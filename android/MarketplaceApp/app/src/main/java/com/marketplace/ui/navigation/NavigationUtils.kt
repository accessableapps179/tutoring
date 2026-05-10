// app/src/main/java/com/marketplace/ui/navigation/NavigationUtils.kt
package com.marketplace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.marketplace.Session

/**
 * Debounces rapid taps — prevents double-navigation on fast fingers.
 * Wrapped in remember() so the closure is stable across recompositions
 * and does not reset its timer on every redraw.
 */
@Composable
fun rememberSingleClick(onClick: () -> Unit): () -> Unit {
    return remember {
        var lastClickTime = 0L
        {
            val now = System.currentTimeMillis()
            if (now - lastClickTime > 1_000L) {
                lastClickTime = now
                onClick()
            }
        }
    }
}

/**
 * Navigates to the home screen (TeacherListScreen) using the current Session,
 * clearing the back stack back to login so the user can't go back.
 */
fun NavController.navigateHome() {
    navigate("teacher_list/${Session.token}/${Session.role}/${Session.userId}/${Session.name}") {
        popUpTo("login") { inclusive = true }
    }
}