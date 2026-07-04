package com.xentoryxlabs.auth

import com.xentoryxlabs.auth.models.*

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class AuthService(
    private val userRepository: UserRepository,
    private val tokenRepository: VerificationTokenRepository,
    private val jwtSecret: String = "secret",
    private val jwtIssuer: String = "https://jwt-provider-domain/",
    private val jwtAudience: String = "jwt-audience"
) {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }

    /**
     * Signs up a new user, hashes the password, generates a 6-digit OTP code, 
     * and saves the verification token in the database.
     * 
     * @return Pair of User and the generated OTP code, or null if username/email already exists
     */
    suspend fun signup(username: String, email: String, password: String): Pair<User, String>? {
        if (userRepository.findByUsername(username) != null) return null
        if (userRepository.findByEmail(email) != null) return null

        val hashedPassword = hashPassword(password)
        val userId = UUID.randomUUID().toString()
        
        val user = User(
            id = userId,
            username = username,
            email = email,
            passwordHash = hashedPassword,
            isVerified = false
        )

        val userCreated = userRepository.create(user)
        if (!userCreated) return null

        // Generate a 6-digit OTP (e.g., "123456")
        val otpCode = (100000..999999).random().toString()
        val tokenId = UUID.randomUUID().toString()
        val expiresAt = System.currentTimeMillis() + 600_000 // 10 minutes expiry

        val verificationToken = VerificationToken(
            id = tokenId,
            userId = userId,
            code = otpCode,
            type = VerificationType.EMAIL_VERIFICATION,
            expiresAt = expiresAt
        )

        val tokenSaved = tokenRepository.create(verificationToken)
        if (!tokenSaved) return null

        // In development, print the OTP to the console since we don't have an SMTP server configured
        println("--- DEVELOPER OTP FOR $username: $otpCode ---")

        return Pair(user, otpCode)
    }

    /**
     * Verifies the OTP code submitted by the user.
     * If valid and not expired, marks the user account as verified and deletes the token.
     */
    suspend fun verifyOtp(code: String): Boolean {
        val verificationToken = tokenRepository.findByCode(code) ?: return false
        
        // Check if token has expired
        if (System.currentTimeMillis() > verificationToken.expiresAt) {
            tokenRepository.delete(verificationToken.id)
            return false
        }

        // Mark user as verified
        val userVerified = userRepository.verifyUser(verificationToken.userId)
        if (!userVerified) return false

        // Clean up the verification token
        tokenRepository.delete(verificationToken.id)
        return true
    }

    /**
     * Authenticates a user by username and password.
     * Returns a signed JWT token if authentication succeeds.
     */
    suspend fun login(username: String, password: String): String? {
        val user = userRepository.findByUsername(username) ?: return null
        
        // Verify password
        if (!verifyPassword(password, user.passwordHash)) return null
        
        // Verify user account is activated
        if (!user.isVerified) return null

        return generateToken(user.id)
    }

    private fun generateToken(userId: String): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000)) // 1 hour expiration
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}
