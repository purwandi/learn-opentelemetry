package com.purwandi.product.config;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

@Component
public class TracingConfiguration {

    String SERVICE_NAME = "product.backend";

    OpenTelemetrySdk instance;

    public TracingConfiguration() {
        System.out.println("init tracing config");

        JaegerGrpcSpanExporter exporter = JaegerGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:14250")
            .build();

        Resource resource = Resource.create(
            Attributes.of(ResourceAttributes.SERVICE_NAME, SERVICE_NAME)
        );

        BatchSpanProcessor processor = BatchSpanProcessor.builder(exporter)
            .setMaxExportBatchSize(512)
            .setMaxQueueSize(2048)
            .setExporterTimeout(30, TimeUnit.SECONDS)
            .setScheduleDelay(5, TimeUnit.SECONDS)
            .build();

        SdkTracerProvider provider = SdkTracerProvider.builder()
            .addSpanProcessor(processor)
            .setSampler(Sampler.traceIdRatioBased(1.0))
            .setResource(resource)
            .build();

        instance = OpenTelemetrySdk.builder()
            .setTracerProvider(provider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();

        Runtime.getRuntime().addShutdownHook(new Thread(provider::close));
    }

    public Tracer getTracer() {
        return this.instance.getTracer(SERVICE_NAME);
    }

    public ContextPropagators getPropagators() {
        return this.instance.getPropagators();
    }

}
