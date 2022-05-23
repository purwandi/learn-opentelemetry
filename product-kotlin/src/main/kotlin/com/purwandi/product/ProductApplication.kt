package com.purwandi.product

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.Banner
import com.purwandi.opentelemetry.*


@SpringBootApplication
class ProductApplication

fun main(args: Array<String>) {
    // initTracer("http://localhost:14278/api/traces")
    runApplication<ProductApplication>(*args) {
      setBannerMode(Banner.Mode.OFF)
    }
    // val trace = context.getBean(OpenTelemetryTracer::class)
}
