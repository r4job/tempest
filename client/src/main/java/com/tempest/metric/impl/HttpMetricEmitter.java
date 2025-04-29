package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpMetricEmitter implements MetricEmitter {

    private static final String POST = "POST";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String FORMAT_STRING = "{\"objectType\":\"%s\",\"itemId\":\"%s\",\"timestamp\":%d,\"count\":%d}";
    private static final String ERROR_MESSAGE_PREFIX = "Failed to send metric to HTTP endpoint: ";
    private final String endpoint;

    public HttpMetricEmitter(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void emit(MetricEvent event) {
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

            conn.getInputStream().close();
            conn.disconnect();
        } catch (Exception e) {
            System.err.println(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }
}
