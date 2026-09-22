package com.xnaver.project.dto;
import java.util.Map;
public record AuthResponse(boolean success, String code, String message,
                           Map<String, String> fieldErrors, String redirectUrl) {
    public static AuthResponse ok(String message, String redirectUrl) {
        return new AuthResponse(true, "OK", message, Map.of(), redirectUrl);
    }
    public static AuthResponse error(String code, String message, Map<String, String> fields) {
        return new AuthResponse(false, code, message, fields, null);
    }
}