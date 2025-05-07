package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class HttpMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(HttpMetricEmitter.class);

    private static final String POST = "POST";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String FORMAT_STRING = "{\"objectType\":\"%s\",\"itemId\":\"%s\",\"timestamp\":%d,\"count\":%d}";
    private final String endpoint;

    public HttpMetricEmitter(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(POST);
            conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
            conn.setDoOutput(true);

            String json = String.format(FORMAT_STRING,
                    event.getObjectType(), event.getItemId(), event.getTimestamp(), event.getCount());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            int statusCode = conn.getResponseCode();
            InputStream responseStream = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody = new BufferedReader(new InputStreamReader(responseStream))
                    .lines()
                    .reduce("", (acc, line) -> acc + line + "\n");

            conn.disconnect();

            if (statusCode >= 200 && statusCode < 300) {
                return CompletableFuture.completedFuture(EmitResult.ok());
            }

            String message = String.format("HTTP %d: %s", statusCode, responseBody.trim());
            logger.error("[HttpMetricEmitter] Failed: {}", message);
            return CompletableFuture.completedFuture(EmitResult.fail(message));

        } catch (Exception e) {
            String message = "Emit error: " + e.getMessage();
            logger.error("[HttpMetricEmitter] Exception occurred: {}", message);
            return CompletableFuture.completedFuture(EmitResult.fail(message));
        }
    }
}
