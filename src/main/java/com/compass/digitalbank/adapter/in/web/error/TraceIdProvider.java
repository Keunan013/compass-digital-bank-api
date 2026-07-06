package com.compass.digitalbank.adapter.in.web.error;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Component;

@Component
public class TraceIdProvider {

    private final Tracer tracer;

    public TraceIdProvider(Tracer tracer) {
        this.tracer = tracer;
    }

    public String currentTraceId() {
        Span span = tracer.currentSpan();
        return span == null ? null : span.context().traceId();
    }
}
