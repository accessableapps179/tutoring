# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A two-sided tutoring marketplace with an Android client and a Kotlin/Ktor backend. Students browse and book trial lessons with teachers; after a trial, either side can unlock ongoing contact. Video calls are peer-to-peer WebRTC, brokered through a custom signaling server.

```
tutoring/
├── android/MarketplaceApp/          # Jetpack Compose Android app
└── backend/teacher-marketplace-backend/  # Ktor REST + WebSocket server
```

## Backend

### Commands

Run from `backend/teacher-marketplace-backend/`:

```bash
./gradlew run          # start server on :8080
./gradlew test         # run tests
./gradlew build        # compile + test
./gradlew buildFatJar  # fat JAR for deployment
```

### Stack

- **Ktor 3.4.0** on Netty, JVM 21
- **Exposed ORM** (0.41.1) + **HikariCP** against **PostgreSQL** (`localhost:5432/marketplace`)
- **JWT** (com.auth0:java-jwt) + **BCrypt** for auth
- **Firebase Admin SDK** for FCM push notifications
- **kotlinx.serialization** for JSON

### Architecture

Every domain concept follows: `routes/` → `service/` → `repository/` → `domain/`

- `Application.kt` — wires everything: DB init, FCM init, JWT config, all routes
- `infrastructure/DatabaseFactory.kt` — connects to Postgres and calls `SchemaUtils.create` + `createMissingTablesAndColumns` on startup; `nuke()` drops and recreates all tables (dev tool)
- `infrastructure/JwtConfig.kt` — installs the `auth-jwt` authentication plugin
- `service/AuthService.kt` — registration, login, password change; JWT secret is hardcoded (`JWT_SECRET`)
- `routes/WebRtcRoutes.kt` — WebSocket signaling: `/lobby/{roomId}` (coordination) and `/signal/{roomId}` (P2P relay); call rooms are in-memory `ConcurrentHashMap`

### Database tables

`TeacherTable`, `UserTable`, `BookingTable`, `WeeklySlotTable`, `AvailabilityOverrideTable`, `TeacherHourRangeTable`, `ContactTable`, `MessageTable`, `PaymentCardTable`, `FcmTokenTable`, `TrialResultTable`, `LedgerTable`

Availability is modelled in three tables: recurring `WeeklySlot` (dayOfWeek + hour), date-specific `AvailabilityOverride` (isAvailable flag), and `TeacherHourRange` (startHour/endHour bounds per teacher).

### Postgres setup

The DB URL, user, and password are hardcoded in `DatabaseFactory.kt`:
```
jdbc:postgresql://localhost:5432/marketplace  user=postgres  password=postal1
```

Create the database before first run: `createdb -U postgres marketplace`

---

## Android App

### Commands

Run from `android/MarketplaceApp/`:

```bash
./gradlew assembleDebug        # build debug APK
./gradlew test                 # unit tests
./gradlew connectedAndroidTest # instrumented tests (needs device/emulator)
```

Build and install via Android Studio or `./gradlew installDebug`.

### Stack

- **Jetpack Compose** + **Navigation Compose** (string-based routes)
- **Retrofit2** + **OkHttp** + `retrofit2-kotlinx-serialization-converter` for HTTP
- **Firebase Messaging** (FCM) for push notifications
- Min SDK 26, Target SDK 36, Kotlin 2.0.21

### Architecture

`ui/screens/` → `viewModel/` → `repository/` → `api/`

- `Session.kt` — in-memory singleton holding auth state (`token`, `role`, `userId`, `name`) and navigation-passing state (`selectedTeacher`, `pendingCallName`, `pendingTeacherName`, `lastSearchResults`). Populated on login, cleared on logout.
- `api/RetrofitClient.kt` — singleton managing all Retrofit API instances. Supports two base URLs: emulator (`10.0.2.2:8080`) and real device (`192.168.0.95:8080`), toggled at runtime via `setUseRealDevice()`. All instances are rebuilt when the token or host changes.
- `ui/navigation/AppNavGraph.kt` — single `NavHost` defining all routes.

### Navigation conventions

- **Only UUIDs/JWTs go in URL path segments.** Display names, `TeacherDto` objects, and other free-text values are stored in `Session` before navigating and read back inside the destination composable. This avoids URL-encoding issues.
- Screen-to-screen data flow: set `Session.selectedTeacher`, `Session.pendingCallName`, or `Session.pendingTeacherName` immediately before `navController.navigate(...)`.
- Guards against process-death session loss: if a screen detects its required `Session` field is empty, it calls `navController.popBackStack()` instead of crashing.

### WebRTC call flow

1. Caller stores `Session.pendingCallName` and navigates to `lobby/{contactId}`.
2. `LobbyScreen` connects to `ws://{host}:8080/lobby/{contactId}`; both sides receive `peer_arrived` when 2 peers join.
3. Both navigate to `video_call/{contactId}` (or `video_call_trial/...` for trial lessons).
4. `VideoCallScreen` connects to `ws://{host}:8080/signal/{contactId}` for WebRTC signaling relay.
5. After a trial call ends, the STUDENT is routed to `TrialResultScreen`; a happy result unlocks an ongoing `Contact`.

### Roles

`STUDENT` and `TEACHER` (stored in JWT and `Session.role`). The `admin` screen is accessible from the login screen without authentication.
