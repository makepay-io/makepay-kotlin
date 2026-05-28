package io.makepay

public class MakePayException(
    message: String,
    public val statusCode: Int? = null,
    public val responseBody: String? = null,
) : RuntimeException(message)
