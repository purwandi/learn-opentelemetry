package com.purwandi.product.pkg.telemetry;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.opentelemetry.context.Context;

@Component
@RequestScope
public class TracingContext {

    @Autowired
    private HttpServletRequest request;

    Context context;

    Boolean extracted = false;

    void extract(HttpHeaders headers) {
        context = Tracing.extractContect(headers);
        extracted = true;
    }

    HttpHeaders inject(HttpHeaders headers) {
        if (!extracted) {
            // if we can not get the current http request by spring beans, use it
            // getCurrentHttpRequest().ifPresent(currentRequest -> request = currentRequest);
            if (request == null) {
                return headers;
            }

            // get headers from request. according to
            // https://stackoverflow.com/questions/25247218/servlet-filter-how-to-get-all-the-headers-from-servletrequest
            extract(Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                    Function.identity(),
                    h -> Collections.list(request.getHeaders(h)),
                    (a, b) -> b,
                    HttpHeaders::new
                ))
            );
        }

        return Tracing.injectHeaders(context, headers);
    }

    // get the current http request by a static way. according to
    // https://stackoverflow.com/questions/592123/is-there-a-static-way-to-get-the-current-httpservletrequest-in-spring
    private static Optional<HttpServletRequest> getCurrentHttpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }
}
