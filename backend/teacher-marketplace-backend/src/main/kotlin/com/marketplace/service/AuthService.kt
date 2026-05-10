// IntelliJ
// src/main/kotlin/com/marketplace/service/AuthService.kt
package com.marketplace.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.marketplace.domain.User
import com.marketplace.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import java.util.Date
import java.util.UUID

class AuthService(private val userRepository: UserRepository = UserRepository()) {

    companion object {
        const val JWT_SECRET = "your-super-secret-key-change-this-later"
        const val JWT_ISSUER = "teacher-marketplace"
        const val JWT_EXPIRY_MS = 86_400_000L
    }

    data class LoginResult(
        val token: String,
        val userId: String,
        val email: String,
        val role: String,
        val name: String
    )

    fun register(email: String, password: String, role: String, name: String): User? {
        val existing = userRepository.findByEmail(email.lowercase())
        if (existing != null) return null

        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        val user = User(
            id = UUID.randomUUID().toString(),
            email = email.lowercase(),
            passwordHash = hashedPassword,
            role = role,
            name = name
        )

        return userRepository.save(user)
    }

    fun login(email: String, password: String): LoginResult? {
        val user = userRepository.findByEmail(email.lowercase()) ?: return null

        val passwordMatches = BCrypt.checkpw(password, user.passwordHash)
        if (!passwordMatches) return null

        val token = JWT.create()
            .withIssuer(JWT_ISSUER)
            .withSubject(user.id)
            .withClaim("email", user.email)
            .withClaim("role", user.role)
            .withClaim("name", user.name)
            .withExpiresAt(Date(System.currentTimeMillis() + JWT_EXPIRY_MS))
            .sign(Algorithm.HMAC256(JWT_SECRET))

        return LoginResult(
            token = token,
            userId = user.id,
            email = user.email,
            role = user.role,
            name = user.name
        )
    }

    fun changePassword(userId: String, currentPassword: String, newPassword: String): Boolean {
        val user = userRepository.findById(userId)
        println("DEBUG changePassword: userId=$userId user=${user?.email} currentPassword=$currentPassword")
        if (user == null) {
            println("DEBUG changePassword: user not found")
            return false
        }
        val passwordMatches = BCrypt.checkpw(currentPassword, user.passwordHash)
        println("DEBUG changePassword: passwordMatches=$passwordMatches hash=${user.passwordHash}")
        if (!passwordMatches) return false
        val newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        return userRepository.updatePassword(userId, newHash)
    }
}