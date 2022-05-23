package com.purwandi.product

// import com.purwandi.product.Tracing.Companion.extractToContext
import io.opentelemetry.context.Context
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

@Component
@RequestScope
class TracingContext {
    @Autowired
    private val request: HttpServletRequest? = null
    var context: Context? = null
    var extracted = false
    fun extract(headers: HttpHeaders?) {
        context = Tracing.extractToContext(headers)
        extracted = true
    }

    fun inject(headers: HttpHeaders?): HttpHeaders? {
        if (!extracted) {
            // if we can not get the current http request by spring beans, use it
//            getCurrentHttpRequest().ifPresent(currentRequest -> request = currentRequest);
            if (request == null) {
                return headers
            }
            // get headers from request. according to https://stackoverflow.com/questions/25247218/servlet-filter-how-to-get-all-the-headers-from-servletrequest
            extract(
                Collections.list(request.headerNames)
                    .stream()
                    .collect(
                        Collectors.toMap(
                            Function.identity(),
                            Function<String, List<String?>> { h: String? -> Collections.list(request.getHeaders(h)) },
                            BinaryOperator { oldValue: List<String?>?, newValue: List<String?> -> newValue },
                            Supplier<HttpHeaders> { HttpHeaders() })
                    )
            )
        }
        return Tracing.injectHeaders(context, headers)
    }

    companion object {
        // get the current http request by a static way. according to https://stackoverflow.com/questions/592123/is-there-a-static-way-to-get-the-current-httpservletrequest-in-spring
        private val currentHttpRequest: Optional<HttpServletRequest>
            private get() = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter { obj: RequestAttributes? -> ServletRequestAttributes::class.java.isInstance(obj) }
                .map { obj: RequestAttributes? -> ServletRequestAttributes::class.java.cast(obj) }
                .map { obj: ServletRequestAttributes -> obj.request }
    }
}
