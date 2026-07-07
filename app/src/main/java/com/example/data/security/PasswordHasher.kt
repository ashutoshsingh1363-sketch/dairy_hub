package com.example.data.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted password hashing using PBKDF2WithHmacSHA256.
 *
 * Replaces the previous plaintext password storage/comparison. Uses only
 * Android's built-in javax.crypto APIs, so no new Gradle dependency is
 * required.
 *
 * Stored format (kept in UserEntity.passwordHash): "<iterations>:<saltBase64>:<hashBase64>"
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    /** Hashes a plaintext password, generating a new random salt. Store the result as-is. */
    fun hash(plainPassword: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hashBytes = pbkdf2(plainPassword, salt, ITERATIONS)
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(hashBytes, Base64.NO_WRAP)
        return "$ITERATIONS:$saltB64:$hashB64"
    }

    /** Verifies a plaintext password against a stored "iterations:salt:hash" string. */
    fun verify(plainPassword: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 3) return false // malformed / legacy plaintext value
        val iterations = parts[0].toIntOrNull() ?: return false
        val salt = try { Base64.decode(parts[1], Base64.NO_WRAP) } catch (e: Exception) { return false }
        val expectedHash = parts[2]
        val actualHashBytes = pbkdf2(plainPassword, salt, iterations)
        val actualHash = Base64.encodeToString(actualHashBytes, Base64.NO_WRAP)
        return constantTimeEquals(actualHash, expectedHash)
    }

    /** True if a stored value is in the new salted-hash format (vs. old plaintext accounts). */
    fun isHashed(stored: String): Boolean = stored.split(":").size == 3 && stored.split(":")[0].toIntOrNull() != null

    private fun pbkdf2(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    // Avoids timing side-channel attacks when comparing hashes.
    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].code xor b[i].code)
        return result == 0
    }
}
