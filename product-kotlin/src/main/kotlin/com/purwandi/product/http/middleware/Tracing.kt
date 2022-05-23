package com.purwandi.product.http.middleware

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.extension.trace.propagation.B3Propagator
import org.springframework.http.HttpHeaders

import java.net.URLConnection;

// https://gist.github.com/pangsq/a615e4143a6564e173a84a27aea0ab8b
class Tracing {
    val openTelemetry: OpenTelemetry = OpenTelemetry.propagating(ContextPropagators.create(
        TextMapPropagator.composite(B3Propagator.injectingMultiHeaders())
    ))

    val getter: TextMapGetter<HttpHeaders?> = object : TextMapGetter<HttpHeaders?>() {

    }

    httpURLConnectionSetter: TextMapSetter<HttpURLConnection> = URLConnection::setRequestProperty
    headerSetter: TextMapSetter<HttpHeaders> = HttpHeaders::set

    companion object {
        fun extractToContext(headers: HttpHeaders) : Context {
            return openTelemetry
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), headers, getter)
        }
    }
}
