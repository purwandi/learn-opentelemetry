package com.purwandi.product.http.middleware;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.purwandi.product.config.TracingConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;

@Component
public class HttpRequestMiddleware extends OncePerRequestFilter {

    @Autowired
    TracingConfiguration tracer;

    private final class TextMapGetterImplementation implements TextMapGetter<HttpHeaders> {
        @Override
        public Iterable<String> keys(HttpHeaders carrier) {
            return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(HttpHeaders carrier, String key) {
            List<String> values = carrier != null ? carrier.get(key) : null;
            if (values == null || values.isEmpty()) {
                return null;
            }

            String value = values.get(0);
            System.out.println(key + " : " + value);
            return value;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain filterChain) throws ServletException, IOException {

        HttpHeaders httpHeaders = Collections
            .list(httpRequest.getHeaderNames())
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                h -> Collections.list(httpRequest.getHeaders(h)),
                (oldValue, newValue) -> newValue,
                HttpHeaders::new
            ));

        TextMapGetter<HttpHeaders> getter = new TextMapGetterImplementation();
        Context context = tracer.getPropagators()
            .getTextMapPropagator()
            .extract(Context.current(), httpHeaders, getter);

        Span span = tracer.getTracer()
            .spanBuilder(httpRequest.getRequestURI() + " " + httpRequest.getMethod())
            .setSpanKind(SpanKind.SERVER)
            .setParent(context)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("component", httpRequest.getScheme());
            span.setAttribute("http.method", httpRequest.getMethod());
            span.setAttribute("http.scheme", "localhost:" + httpRequest.getLocalPort());
            span.setAttribute("http.target", httpRequest.getRequestURI());

            filterChain.doFilter(httpRequest, httpResponse);
        } catch (Exception e) {
            span.setAttribute("http.status_code", httpResponse.getStatus());
            span.setStatus(StatusCode.ERROR);
        } finally {
            span.end();
        }
    }
}
