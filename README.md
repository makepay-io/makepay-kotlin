# MakePay Kotlin SDK

Kotlin JVM SDK for MakePay server-side integrations.

Use it to create payment links, fetch payment links, upsert customers, build hosted or embedded checkout URLs, work with MakePay bookkeeping, and verify signed webhooks.

## Requirements

- Kotlin `2.3.21`
- JDK 17 or newer
- Maven 3.9 or newer

## Installation

The package is planned for Maven Central as:

```kotlin
implementation("io.makepay:makepay-kotlin:0.1.0")
```

Until the first release is published, reference this repository as source or publish the snapshot to your internal Maven registry.

## Quick Start

```kotlin
import io.makepay.CreatePaymentLinkOptions
import io.makepay.MakePayClient
import io.makepay.MakePayOptions
import io.makepay.PaymentLinkPayload

val makePay = MakePayClient(
    MakePayOptions(
        keyId = System.getenv("MAKEPAY_KEY_ID"),
        keySecret = System.getenv("MAKEPAY_KEY_SECRET"),
        webhookSecret = System.getenv("MAKEPAY_WEBHOOK_SECRET"),
    ),
)

val created = makePay.createPaymentLink(
    payload = PaymentLinkPayload(
        title = "Order #1042",
        amount = "129.99",
        currency = "USDT",
        customerEmail = "buyer@example.com",
        successUrl = "https://merchant.example/orders/1042/success",
        failureUrl = "https://merchant.example/orders/1042/pay",
    ),
    createOptions = CreatePaymentLinkOptions(sendPaymentRequestEmail = false),
)

println(created)
```

## Checkout URLs

```kotlin
val hosted = makePay.buildHostedCheckoutUrl("pay_123")
val embedded = makePay.buildEmbeddedCheckoutUrl(
    paymentUid = "pay_123",
    parentOrigin = "https://merchant.example",
)
```

## Webhooks

Verify the exact raw body before parsing JSON:

```kotlin
val trusted = MakePayWebhookVerifier.verify(
    rawBody = requestBodyBytes,
    signatureHeader = requestHeaders["X-MakePay-Signature"],
    webhookSecret = System.getenv("MAKEPAY_WEBHOOK_SECRET"),
)
```

The verifier expects the `t=<timestamp>,v1=<hex>` signature format and applies a five-minute tolerance by default.

## Bookkeeping

```kotlin
val summary = makePay.getBookkeepingSummary()
val paymentLink = makePay.createBookkeepingInvoicePaymentLink(
    invoiceId = "inv_123",
    sendPaymentRequestEmail = true,
)
```

For flexible bookkeeping payloads, pass `JsonObject` values from `kotlinx.serialization.json`.

## Validation

```bash
mvn -B verify
```

Maintainer: Ethan Carter (`makepayio`).
