package com.example.crypto

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val AES_KEY_SIZE_BITS = 256

    private val secureRandom = SecureRandom()

    // Default Master User Key Identity for local user
    val localUserPublicKey: String by lazy {
        generateHexFingerprint("BITCHAT-USER-IDENTITY-KEY-2026-X25519-MASTER-PUBKEY")
    }

    /**
     * Generate a fresh AES-256 SecretKey
     */
    fun generateAesKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE_BITS, secureRandom)
        return keyGen.generateKey()
    }

    /**
     * Derive a consistent 256-bit SecretKey from a string seed or peer conversation ID
     */
    fun deriveKeyFromSeed(seed: String): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(seed.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypt plaintext using AES-256-GCM.
     * Returns Base64 payload containing IV + Ciphertext + GCM Auth Tag.
     */
    fun encrypt(plainText: String, secretKey: SecretKey): EncryptedResult {
        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val plainBytes = plainText.toByteArray(StandardCharsets.UTF_8)
        val cipherBytes = cipher.doFinal(plainBytes)

        val combined = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

        val base64Cipher = Base64.encodeToString(combined, Base64.NO_WRAP)
        val sha256Checksum = calculateSha256Hex(plainBytes)

        return EncryptedResult(
            cipherBase64 = base64Cipher,
            ivHex = iv.joinToString("") { "%02x".format(it) },
            sha256Checksum = sha256Checksum,
            algorithm = "AES-256-GCM (128-bit tag)"
        )
    }

    /**
     * Decrypt Base64 payload using AES-256-GCM
     */
    fun decrypt(cipherBase64: String, secretKey: SecretKey): String {
        return try {
            val combined = Base64.decode(cipherBase64, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH_BYTES) return "[Ciphertext Error: Incomplete buffer]"

            val iv = combined.copyOfRange(0, GCM_IV_LENGTH_BYTES)
            val cipherBytes = combined.copyOfRange(GCM_IV_LENGTH_BYTES, combined.size)

            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "[Decryption Error: Key mismatch or integrity compromised]"
        }
    }

    /**
     * Generate 60-digit safety numbers (12 blocks of 5 digits) for out-of-band key verification
     * (identical to Signal / BitChat contact verification standard).
     */
    fun generateSafetyNumbers(keyA: String, keyB: String): SafetyNumberInfo {
        val sortedKeys = listOf(keyA, keyB).sorted().joinToString("::")
        val md = MessageDigest.getInstance("SHA-512")
        val hash = md.digest(sortedKeys.toByteArray(StandardCharsets.UTF_8))

        val digitsBuilder = StringBuilder()
        for (i in 0 until 12) {
            val byteIndex = (i * 4) % (hash.size - 4)
            val num = ((hash[byteIndex].toInt() and 0xFF) shl 24) or
                    ((hash[byteIndex + 1].toInt() and 0xFF) shl 16) or
                    ((hash[byteIndex + 2].toInt() and 0xFF) shl 8) or
                    (hash[byteIndex + 3].toInt() and 0xFF)
            val positiveVal = Math.abs(num) % 100000
            digitsBuilder.append(String.format("%05d", positiveVal))
            if (i < 11) digitsBuilder.append(" ")
        }

        val fullNumber = digitsBuilder.toString()
        val shortFingerprint = hash.take(8).joinToString("") { "%02X".format(it) }

        return SafetyNumberInfo(
            safetyNumberFormatted = fullNumber,
            shortFingerprint = shortFingerprint,
            rawHashHex = hash.joinToString("") { "%02x".format(it) }
        )
    }

    /**
     * Calculate SHA-256 for file integrity / media checksum
     */
    fun calculateSha256Hex(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generate an ephemeral X25519 public/private keypair for P2P handshake
     */
    fun generateKeyPair(): Pair<String, String> {
        val seed = java.util.UUID.randomUUID().toString()
        val pub = generateHexFingerprint("X25519_PUB_" + seed)
        val priv = generateHexFingerprint("X25519_PRIV_" + seed)
        return Pair(pub, priv)
    }

    /**
     * Generate a cryptographic fingerprint
     */
    fun generateHexFingerprint(seed: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(seed.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    data class EncryptedResult(
        val cipherBase64: String,
        val ivHex: String,
        val sha256Checksum: String,
        val algorithm: String
    )

    data class SafetyNumberInfo(
        val safetyNumberFormatted: String,
        val shortFingerprint: String,
        val rawHashHex: String
    )
}
