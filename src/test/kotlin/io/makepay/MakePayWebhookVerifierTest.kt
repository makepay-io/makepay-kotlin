package io.makepay

import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MakePayWebhookVerifierTest {
    @Test
    fun `verify accepts a valid webhook signature`() {
        val body = """{"event":{"type":"payment.paid"}}""".toByteArray()
        val secret = "whsec_test"
        val now = Instant.ofEpochSecond(1_700_000_000)
        val header = headerFor(body, secret, now)

        assertTrue(MakePayWebhookVerifier.verify(body, header, secret, now = now))
    }

    @Test
    fun `verify rejects invalid signatures`() {
        val body = """{"event":{"type":"payment.paid"}}""".toByteArray()
        val secret = "whsec_test"
        val now = Instant.ofEpochSecond(1_700_000_000)
        val header = headerFor(body, secret, now)

        assertFalse(MakePayWebhookVerifier.verify(body, header, "wrong", now = now))
    }

    @Test
    fun `parseVerified returns json after verification`() {
        val body = """{"event":{"type":"payment.paid"}}""".toByteArray()
        val secret = "whsec_test"
        val now = Instant.ofEpochSecond(1_700_000_000)
        val header = headerFor(body, secret, now)

        val parsed = MakePayWebhookVerifier.parseVerified(body, header, secret, now = now)

        assertEquals("payment.paid", parsed["event"]!!.jsonObject["type"]!!.jsonPrimitive.content)
    }

    private fun headerFor(body: ByteArray, secret: String, timestamp: Instant): String {
        val timestampText = timestamp.epochSecond.toString()
        val payload = "$timestampText.".toByteArray() + body
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        val signature = mac.doFinal(payload).joinToString("") { "%02x".format(it.toInt() and 0xff) }

        return "t=$timestampText,v1=$signature"
    }
}
