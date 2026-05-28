package io.makepay

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

public class JdkMakePayTransport(
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
) : MakePayTransport {
    override fun execute(request: MakePayHttpRequest): MakePayHttpResponse {
        val builder = HttpRequest.newBuilder(request.uri)
        request.headers.forEach { (name, value) -> builder.header(name, value) }

        if (request.body == null) {
            builder.method(request.method, HttpRequest.BodyPublishers.noBody())
        } else {
            builder.method(request.method, HttpRequest.BodyPublishers.ofString(request.body))
        }

        val response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        return MakePayHttpResponse(response.statusCode(), response.body() ?: "")
    }
}
