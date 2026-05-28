package io.makepay

import java.net.URI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MakePayClientTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `createPaymentLink sends partner headers and payload`() {
        val transport = CapturingTransport(
            MakePayHttpResponse(201, """{"paymentLink":{"uid":"pay_123"}}"""),
        )
        val client = client(transport)

        val response = client.createPaymentLink(
            payload = PaymentLinkPayload(
                title = "Order #1042",
                amount = "129.99",
                currency = "USDT",
                customerEmail = "buyer@example.com",
            ),
            createOptions = CreatePaymentLinkOptions(sendPaymentRequestEmail = true),
        )

        val request = requireNotNull(transport.request)
        assertEquals("POST", request.method)
        assertEquals(URI("https://api.example/api/partner/v1/makepay/payment-links"), request.uri)
        assertEquals("mk_test", request.headers["X-MakeCrypto-Key-Id"])
        assertEquals("mksec_test", request.headers["X-MakeCrypto-Key-Secret"])

        val body = json.parseToJsonElement(requireNotNull(request.body)).jsonObject
        assertTrue(body["sendPaymentRequestEmail"]!!.jsonPrimitive.boolean)
        assertEquals("Order #1042", body["payload"]!!.jsonObject["title"]!!.jsonPrimitive.content)
        assertEquals("pay_123", response["paymentLink"]!!.jsonObject["uid"]!!.jsonPrimitive.content)
    }

    @Test
    fun `buildHostedCheckoutUrl uses configured checkout host`() {
        val client = client(CapturingTransport(MakePayHttpResponse(200, "{}")))

        assertEquals("https://checkout.example/payment/pay_123", client.buildHostedCheckoutUrl("pay_123"))
    }

    @Test
    fun `api errors raise exception with status and response body`() {
        val client = client(CapturingTransport(MakePayHttpResponse(401, """{"error":"unauthorized"}""")))

        val error = assertThrows(MakePayException::class.java) {
            client.getPaymentLink("pay_123")
        }

        assertEquals(401, error.statusCode)
        assertEquals("""{"error":"unauthorized"}""", error.responseBody)
    }

    private fun client(transport: MakePayTransport): MakePayClient {
        return MakePayClient(
            MakePayOptions(
                keyId = "mk_test",
                keySecret = "mksec_test",
                baseUrl = "https://api.example",
                checkoutBaseUrl = "https://checkout.example",
            ),
            transport,
        )
    }

    private class CapturingTransport(
        private val response: MakePayHttpResponse,
    ) : MakePayTransport {
        var request: MakePayHttpRequest? = null

        override fun execute(request: MakePayHttpRequest): MakePayHttpResponse {
            this.request = request
            return response
        }
    }
}
