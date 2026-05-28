package io.makepay

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

public class MakePayClient(
    private val options: MakePayOptions,
    private val transport: MakePayTransport = JdkMakePayTransport(),
) {
    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    init {
        require(options.keyId.isNotBlank()) { "MakePay keyId is required." }
        require(options.keySecret.isNotBlank()) { "MakePay keySecret is required." }
    }

    public fun createPaymentLink(
        payload: PaymentLinkPayload,
        createOptions: CreatePaymentLinkOptions = CreatePaymentLinkOptions(),
    ): JsonObject {
        return sendJson(
            method = "POST",
            path = "/api/partner/v1/makepay/payment-links",
            body = json.encodeToString(
                CreatePaymentLinkRequest(
                    status = createOptions.status,
                    sendPaymentRequestEmail = createOptions.sendPaymentRequestEmail,
                    payload = payload,
                ),
            ),
        )
    }

    public fun listPaymentLinks(query: Map<String, String?> = emptyMap()): JsonObject {
        return sendJson("GET", "/api/partner/v1/makepay/payment-links", query = query)
    }

    public fun getPaymentLink(uid: String): JsonObject {
        return sendJson("GET", "/api/partner/v1/makepay/payment-links/${escape(uid, "uid")}")
    }

    public fun updatePaymentLink(uid: String, update: PaymentLinkStatusUpdate): JsonObject {
        return sendJson(
            method = "PATCH",
            path = "/api/partner/v1/makepay/payment-links/${escape(uid, "uid")}",
            body = json.encodeToString(update),
        )
    }

    public fun upsertCustomer(payload: CustomerPayload): JsonObject {
        return sendJson(
            method = "POST",
            path = "/api/partner/v1/makepay/customers",
            body = json.encodeToString(payload),
        )
    }

    public fun listDestinationAssets(): JsonObject {
        return sendJson("GET", "/api/partner/v1/makepay/destination-assets")
    }

    public fun getBookkeepingSummary(): JsonObject {
        return sendJson("GET", "/api/partner/v1/makepay/bookkeeping")
    }

    public fun createBookkeepingInvoice(payload: JsonObject): JsonObject {
        return sendJson(
            method = "POST",
            path = "/api/partner/v1/makepay/bookkeeping/invoices",
            body = payload.toString(),
        )
    }

    public fun createBookkeepingInvoicePaymentLink(
        invoiceId: String,
        sendPaymentRequestEmail: Boolean = false,
    ): JsonObject {
        return sendJson(
            method = "POST",
            path = "/api/partner/v1/makepay/bookkeeping/invoices/${escape(invoiceId, "invoiceId")}/payment-link",
            body = json.encodeToString(InvoicePaymentLinkRequest(sendPaymentRequestEmail)),
        )
    }

    public fun createBookkeepingReconciliation(payload: JsonObject): JsonObject {
        return sendJson(
            method = "POST",
            path = "/api/partner/v1/makepay/bookkeeping/reconciliation",
            body = payload.toString(),
        )
    }

    public fun buildHostedCheckoutUrl(paymentUid: String): String {
        return buildUri(options.checkoutBaseUrl, "/payment/${escape(paymentUid, "paymentUid")}").toString()
    }

    public fun buildEmbeddedCheckoutUrl(paymentUid: String, parentOrigin: String? = null): String {
        val query = if (parentOrigin == null) emptyMap() else mapOf("parentOrigin" to parentOrigin)
        return buildUri(options.checkoutBaseUrl, "/embed/payment/${escape(paymentUid, "paymentUid")}", query).toString()
    }

    private fun sendJson(
        method: String,
        path: String,
        body: String? = null,
        query: Map<String, String?> = emptyMap(),
    ): JsonObject {
        val response = transport.execute(
            MakePayHttpRequest(
                method = method,
                uri = buildUri(options.baseUrl, path, query),
                headers = mapOf(
                    "Accept" to "application/json",
                    "Content-Type" to "application/json",
                    "User-Agent" to options.userAgent,
                    "X-MakeCrypto-Key-Id" to options.keyId,
                    "X-MakeCrypto-Key-Secret" to options.keySecret,
                ),
                body = body,
            ),
        )

        if (response.statusCode !in 200..299) {
            throw MakePayException(
                message = "MakePay API request failed with ${response.statusCode}.",
                statusCode = response.statusCode,
                responseBody = response.body,
            )
        }

        return json.parseToJsonElement(response.body.ifBlank { "{}" }).jsonObject
    }

    private fun buildUri(baseUrl: String, path: String, query: Map<String, String?> = emptyMap()): URI {
        val normalizedBase = baseUrl.trimEnd('/') + "/"
        val resolved = URI(normalizedBase).resolve(path.trimStart('/'))
        val queryString = query
            .mapNotNull { (key, value) ->
                if (value == null) null else "${encode(key)}=${encode(value)}"
            }
            .joinToString("&")

        return URI(
            resolved.scheme,
            resolved.authority,
            resolved.path,
            queryString.ifBlank { null },
            null,
        )
    }

    private fun escape(value: String, parameterName: String): String {
        require(value.isNotBlank()) { "$parameterName is required." }
        return encode(value)
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
    }
}
