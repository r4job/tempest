package com.tempest.grpc.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;

import java.util.concurrent.Executor;

public class AuthCallCredentials extends CallCredentials {

    private final String token;

    public AuthCallCredentials(String token) {
        this.token = token;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier applier) {
        Metadata headers = new Metadata();
        Metadata.Key<String> authKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(authKey, "Bearer " + token);
        applier.apply(headers);
    }
}
