package io.makepay

public data class MakePayOptions(
    val keyId: String,
    val keySecret: String,
    val webhookSecret: String = "",
    val baseUrl: String = "https://www.makecrypto.io",
    val checkoutBaseUrl: String = "https://makepay.io",
    val userAgent: String = "MakePayKotlin/0.1.0",
)
