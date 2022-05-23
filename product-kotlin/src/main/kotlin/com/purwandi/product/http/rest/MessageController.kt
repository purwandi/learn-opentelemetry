package com.purwandi.product.rest

import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired
import com.purwandi.product.http.response.MessageResponse;
import com.purwandi.product.http.response.ProductResponse;
import com.purwandi.product.models.Message;
import com.purwandi.product.utils.Telemetry
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.GlobalOpenTelemetry;
@RestController
@RequestMapping("/messages")
class MessageController {

  @Autowired
  lateinit var telemetry: Telemetry

  @GetMapping("/")
  fun index(): String {
    val tracer: Tracer = telemetry.instance().getTracer("opentelemetry-java-sdk")
    val span: Span = tracer.spanBuilder("get").startSpan()

    try {

    } finally {
      span.end();
    }

    return "Hello World";
  }

  @GetMapping("/{id}")
  fun get(): Message {
    val tracer = GlobalOpenTelemetry.get().getTracer("opentelemetry-java-sdk")
    val span: Span = tracer.spanBuilder("get").startSpan()

    println("from controller")

    try {

    } finally {
      span.end();
    }

    return Message("1", "Purwandi")
  }

  @GetMapping("/{id}/messages")
  fun getMessage(): MessageResponse<List<Message>> {
    return MessageResponse(
      listOf(
        Message("1", "Purwandi"),
        Message("2", "Hazel")
      ),
      "error"
    )
  }

  @GetMapping("/{id}/product")
  // fun getProduct(@RequestAttribute("spanTrace") span: Span): ProductResponse<List<Message>, String> {
  fun getProduct(): ProductResponse<List<Message>, String> {
    val tracer = GlobalOpenTelemetry.get().getTracer("opentelemetry-java-sdk")
    val span: Span = tracer.spanBuilder("get").startSpan()

    try {

    } finally {
      span.end();
    }


    return ProductResponse(
      listOf(
        Message("1", "Purwandi"),
        Message("2", "Hazel")
      ),
      "error"
    )
  }
}
