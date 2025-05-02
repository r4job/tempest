package com.tempest.grpc.auth;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TimeBasedKeyProvider {

    private static final String DATE_FORMAT = "yyyyMMddHHmm";
    private final String secretSeed;  // base secret shared between client and server
    private final long rotationIntervalMillis;

    public TimeBasedKeyProvider(String secretSeed, long rotationIntervalMillis) {
        this.secretSeed = Objects.requireNonNull(secretSeed);
        this.rotationIntervalMillis = rotationIntervalMillis;
    }

    public KeyInfo getCurrentKey() {
        long now = System.currentTimeMillis();
        long baseTimestamp = now - (now % rotationIntervalMillis); // floor to nearest rotation interval

        String kid = formatTimestampAsKeyId(baseTimestamp);
        String derivedKey = secretSeed + ":" + kid;
        SecretKey hmacKey = Keys.hmacShaKeyFor(derivedKey.getBytes(StandardCharsets.UTF_8));

        return new KeyInfo(kid, hmacKey);
    }

    private String formatTimestampAsKeyId(long timestampMillis) {
        return "v" + DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochMilli(timestampMillis));
    }

    public record KeyInfo(String kid, SecretKey secret) {}
}
