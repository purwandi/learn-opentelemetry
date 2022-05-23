package com.purwandi.product.http.rest;

import com.purwandi.product.config.TracingConfiguration;
import com.purwandi.product.http.requests.ProductRequest;
import com.purwandi.product.http.response.ProductResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

@RequestMapping("/api/v1/products")
@RestController
public class ProductController {

    @Autowired
    TracingConfiguration tracing;

    @GetMapping("")
    public String GetIndex() {
        Tracer tracer = tracing.getTracer();
        Span span = tracer.spanBuilder("GetIndex").startSpan();

        span.end();
        return "Hello world";
    }

    @GetMapping("/{id}")
    public ProductResponse GetProduct(@PathVariable("id") String id) {
        return new ProductResponse(id, "Product name", "Product description");
    }

    @PostMapping("")
    public ProductResponse Store(@RequestBody ProductRequest request) {
        return new ProductResponse("1", request.getName(), request.getDescription());
    }
}
