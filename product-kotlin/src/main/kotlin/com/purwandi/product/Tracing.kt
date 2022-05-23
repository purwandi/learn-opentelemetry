package com.purwandi.product

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.context.propagation.TextMapSetter
import io.opentelemetry.extension.trace.propagation.B3Propagator
import org.springframework.http.HttpHeaders
import org.springframework.lang.Nullable
import java.net.HttpURLConnection

interface Tracing {
    companion object {
        @JvmStatic
        fun extractToContext(headers: HttpHeaders?): Context {
            return openTelemetry.propagators.textMapPropagator.extract(Context.current(), headers, getter)
        }

        fun extract(headers: HttpHeaders?) {
            val context = extractToContext(headers)
            context.makeCurrent()
        }

        fun injectHttpURLConnection(context: Context?, httpURLConnection: HttpURLConnection?): HttpURLConnection? {
            if (context == null) {
                return httpURLConnection
            }
            openTelemetry.propagators.textMapPropagator.inject(context, httpURLConnection, httpURLConnectionSetter)
            return httpURLConnection
        }

        fun injectHttpURLConnection(httpURLConnection: HttpURLConnection?): HttpURLConnection? {
            val context = Context.current()
            return injectHttpURLConnection(context, httpURLConnection)
        }

        fun injectHeaders(context: Context?, headers: HttpHeaders?): HttpHeaders? {
            if (context == null) {
                return headers
            }
            openTelemetry.propagators.textMapPropagator.inject(context, headers, headersSetter)
            return headers
        }

        fun injectHeaders(headers: HttpHeaders?): HttpHeaders? {
            val context = Context.current()
            return injectHeaders(context, headers)
        }

        val openTelemetry = OpenTelemetry.propagating(
            ContextPropagators.create(TextMapPropagator.composite(B3Propagator.injectingMultiHeaders()))
        )
        val getter: TextMapGetter<HttpHeaders> = object : TextMapGetter<HttpHeaders?> {
            override fun keys(carrier: HttpHeaders?): Iterable<String> {
                return carrier!!.keys
            }

            @Nullable
            override fun get(carrier: HttpHeaders?, key: String): String? {
                val values = carrier?.get(key)
                return if (values == null || values.isEmpty()) {
                    null
                } else values[0]
            }
        }
        val httpURLConnectionSetter = TextMapSetter { obj: HttpURLConnection?, key: String?, value: String? ->
            obj!!.setRequestProperty(
                key,
                value
            )
        }
        val headersSetter = TextMapSetter { obj: HttpHeaders?, headerName: String?, headerValue: String? ->
            obj!![headerName!!] = headerValue
        }
    }
}
