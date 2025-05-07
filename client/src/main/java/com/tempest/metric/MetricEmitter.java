package com.tempest.metric;

import java.util.concurrent.CompletableFuture;

public interface MetricEmitter {
    CompletableFuture<EmitResult> emit(MetricEvent event);
}
