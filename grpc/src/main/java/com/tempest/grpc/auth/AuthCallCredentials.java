package com.tempest.grpc.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.concurrent.Executor;

public class AuthCallCredentials extends CallCredentials {

    private final TimeBasedKeyProvider keyProvider;
    private final long tokenTTLMillis;

    public AuthCallCredentials(TimeBasedKeyProvider keyProvider, long tokenTTLMillis) {
        this.keyProvider = keyProvider;
        this.tokenTTLMillis = tokenTTLMillis;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier applier) {
        try {
            TimeBasedKeyProvider.KeyInfo keyInfo = keyProvider.getCurrentKey();
            long now = System.currentTimeMillis();

            String jwt = Jwts.builder()
                    .header()
                    .add("kid", keyInfo.kid())
                    .and()
                    .subject("metrics-client")
                    .issuedAt(new Date(now))
                    .expiration(new Date(now + tokenTTLMillis))
                    .signWith(keyInfo.secret(), Jwts.SIG.HS256)
                    .compact();

            Metadata headers = new Metadata();
            Metadata.Key<String> authKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
            headers.put(authKey, "Bearer " + jwt);
            applier.apply(headers);

        } catch (Exception e) {
            applier.fail(Status.UNAUTHENTICATED.withDescription("Token signing failed: " + e.getMessage()));
        }
    }
}
