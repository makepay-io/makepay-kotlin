package io.makepay

import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

public object MakePayWebhookVerifier {
    private val json = Json { ignoreUnknownKeys = true }

    public fun verify(
        rawBody: ByteArray,
        signatureHeader: String?,
        webhookSecret: String,
        toleranceSeconds: Long = 300,
        now: Instant = Instant.now(),
    ): Boolean {
        if (signatureHeader.isNullOrBlank() || webhookSecret.isBlank()) {
            return false
        }

        val parts = parseSignatureHeader(signatureHeader)
        val timestampText = parts["t"] ?: return false
        val signatureHex = parts["v1"] ?: return false
        val timestamp = timestampText.toLongOrNull() ?: return false
        val signedAt = Instant.ofEpochSecond(timestamp)

        if (kotlin.math.abs(now.epochSecond - signedAt.epochSecond) > toleranceSeconds) {
            return false
        }

        val actual = decodeHex(signatureHex) ?: return false
        val expected = hmacSha256(webhookSecret, "$timestampText.".toByteArray() + rawBody)

        return MessageDigest.isEqual(expected, actual)
    }

    public fun parseVerified(
        rawBody: ByteArray,
        signatureHeader: String?,
        webhookSecret: String,
        toleranceSeconds: Long = 300,
        now: Instant = Instant.now(),
    ): JsonObject {
        if (!verify(rawBody, signatureHeader, webhookSecret, toleranceSeconds, now)) {
            throw MakePayException("Invalid MakePay webhook signature.")
        }

        return json.parseToJsonElement(rawBody.decodeToString()).jsonObject
    }

    private fun parseSignatureHeader(header: String): Map<String, String> {
        return header.split(",")
            .mapNotNull { segment ->
                val separator = segment.indexOf('=')
                if (separator <= 0 || separator == segment.lastIndex) {
                    null
                } else {
                    segment.substring(0, separator).trim() to segment.substring(separator + 1).trim()
                }
            }
            .toMap()
    }

    private fun hmacSha256(secret: String, payload: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(payload)
    }

    private fun decodeHex(value: String): ByteArray? {
        if (value.isEmpty() || value.length % 2 != 0) {
            return null
        }

        return try {
            value.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        } catch (_: NumberFormatException) {
            null
        }
    }
}
