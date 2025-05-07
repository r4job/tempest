package com.tempest.metric;

import java.util.regex.Matcher;

public class EmitResult {
    private final boolean success;
    private final String message;

    public EmitResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public static EmitResult ok() {
        return new EmitResult(true, "OK");
    }

    public static EmitResult fail(String format, Object... args) {
        String message = formatMessage(format, args);
        return new EmitResult(false, message);
    }

    private static String formatMessage(String format, Object... args) {
        for (Object arg : args) {
            format = format.replaceFirst("\\{}", Matcher.quoteReplacement(String.valueOf(arg)));
        }
        return format;
    }
}
