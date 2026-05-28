package io.makepay

import java.net.URI

public data class MakePayHttpRequest(
    val method: String,
    val uri: URI,
    val headers: Map<String, String>,
    val body: String? = null,
)

public data class MakePayHttpResponse(
    val statusCode: Int,
    val body: String = "",
)

public fun interface MakePayTransport {
    public fun execute(request: MakePayHttpRequest): MakePayHttpResponse
}
