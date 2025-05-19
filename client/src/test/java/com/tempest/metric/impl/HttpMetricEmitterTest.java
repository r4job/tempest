package com.tempest.metric.impl;

import com.sun.net.httpserver.HttpServer;
import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class HttpMetricEmitterTest {

    private static HttpServer server;
    private static int port;

    @BeforeAll
    static void startMockServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();

        server.createContext("/emit", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        server.createContext("/fail", exchange -> {
            String response = "Bad Request";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        server.start();
    }

    @AfterAll
    static void stopMockServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void testEmitSuccess() {
        HttpMetricEmitter emitter = new HttpMetricEmitter("http://localhost:" + port + "/emit");
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        CompletableFuture<EmitResult> result = emitter.emit(event);

        assertTrue(result.join().isSuccess());
    }

    @Test
    void testEmitFailureResponse() {
        HttpMetricEmitter emitter = new HttpMetricEmitter("http://localhost:" + port + "/fail");
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        CompletableFuture<EmitResult> result = emitter.emit(event);

        assertFalse(result.join().isSuccess());
        assertTrue(result.join().getMessage().contains("HTTP 400"));
    }

    @Test
    void testEmitConnectionError() {
        HttpMetricEmitter emitter = new HttpMetricEmitter("http://localhost:9999/unreachable");
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        CompletableFuture<EmitResult> result = emitter.emit(event);

        assertFalse(result.join().isSuccess());
        assertTrue(result.join().getMessage().toLowerCase().contains("emit error"));
    }
}
