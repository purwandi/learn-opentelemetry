package com.purwandi.product.utils;

import org.springframework.stereotype.Component;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.concurrent.TimeUnit;

@Component
class Telemetry {
  init {
    println("init block")
  }

  var name: String
  var tracer: OpenTelemetry

  constructor() {
    println("init construct telemetry")
    name = "init from telemetry instance"

    val exporter: JaegerGrpcSpanExporter = JaegerGrpcSpanExporter.builder()
      .setEndpoint("http://localhost:14250")
      .build();

    val resource: Resource = Resource.create(
      Attributes.of(
        ResourceAttributes.SERVICE_NAME, "opentelemetry-java-sdk",
      )
    )

    val batchSpanProcessor: BatchSpanProcessor = BatchSpanProcessor.builder(exporter)
      .setMaxExportBatchSize(512)
      .setMaxQueueSize(2048)
      .setExporterTimeout(30, TimeUnit.SECONDS)
      .setScheduleDelay(5, TimeUnit.SECONDS)
      .build();

    val provider: SdkTracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(batchSpanProcessor)
      .setSampler(Sampler.traceIdRatioBased(1.0))
      .setResource(resource)
      .build();

    tracer = OpenTelemetrySdk.builder()
      .setTracerProvider(provider)
      .buildAndRegisterGlobal();

    // shut down the SDK cleanly at JVM exit.
    Runtime.getRuntime().addShutdownHook(Thread {
      provider.shutdown();
    });
  }

  fun instance(): OpenTelemetry {
    return tracer
  }
}
