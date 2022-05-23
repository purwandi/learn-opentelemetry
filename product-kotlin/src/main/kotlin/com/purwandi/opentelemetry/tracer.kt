package com.purwandi.opentelemetry;

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

class Tracer {
  init {
    println("init block")
  }

  constructor() {
    println("tracer constructed")
  }
}

fun initTracer(jaegerEndpoint: String): OpenTelemetry {
  val exporter: JaegerGrpcSpanExporter = JaegerGrpcSpanExporter.builder().setEndpoint(jaegerEndpoint).build();
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

  val trace: OpenTelemetry = OpenTelemetrySdk.builder()
    .setTracerProvider(provider)
    .buildAndRegisterGlobal();

  // shut down the SDK cleanly at JVM exit.
  Runtime.getRuntime().addShutdownHook(Thread {
    provider.shutdown();
  });

  System.out.println("init tracer");

  return trace;
}
