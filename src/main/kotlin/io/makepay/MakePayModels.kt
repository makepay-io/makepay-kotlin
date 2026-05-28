package io.makepay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class CreatePaymentLinkOptions(
    val status: String = "active",
    val sendPaymentRequestEmail: Boolean = false,
)

@Serializable
internal data class CreatePaymentLinkRequest(
    val status: String = "active",
    val sendPaymentRequestEmail: Boolean = false,
    val payload: PaymentLinkPayload,
)

@Serializable
public data class PaymentLinkPayload(
    val title: String? = null,
    val label: String? = null,
    val description: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val asset: String? = null,
    val orderId: String? = null,
    val merchantOrderId: String? = null,
    val customerEmail: String? = null,
    val receiptEmail: String? = null,
    val clientId: String? = null,
    val returnUrl: String? = null,
    val successUrl: String? = null,
    val failureUrl: String? = null,
    val expirationTime: String? = null,
    val metadata: JsonObject? = null,
)

@Serializable
public data class CustomerPayload(
    val email: String? = null,
    val customerEmail: String? = null,
    val name: String? = null,
    val clientId: String? = null,
    val metadata: JsonObject? = null,
)

@Serializable
public data class PaymentLinkStatusUpdate(
    val status: String = "active",
)

@Serializable
internal data class InvoicePaymentLinkRequest(
    val sendPaymentRequestEmail: Boolean = false,
)

@Serializable
public data class MakePayWebhookEvent(
    val type: String? = null,
    @SerialName("event")
    val nestedEvent: JsonObject? = null,
)
