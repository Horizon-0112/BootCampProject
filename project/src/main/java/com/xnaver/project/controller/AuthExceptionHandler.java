package com.xnaver.project.controller;
import com.xnaver.project.dto.AuthResponse;
import com.xnaver.project.service.AuthValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestControllerAdvice(assignableTypes = AuthApiController.class)
public class AuthExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);
    @ExceptionHandler(AuthValidationException.class)
    public ResponseEntity<AuthResponse> validation(AuthValidationException e) {
        return ResponseEntity.badRequest().body(AuthResponse.error("VALIDATION_ERROR", e.getMessage(), e.getFieldErrors()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AuthResponse> conflict(DataIntegrityViolationException e) {
        log.error("Authentication data constraint violation", e);
        return ResponseEntity.status(409).body(AuthResponse.error("CONFLICT",
                "가입 정보를 저장하지 못했습니다. 이메일 또는 닉네임 중복 여부를 확인해 주세요.", Map.of()));
    }
    @ExceptionHandler({MailException.class, IllegalStateException.class})
    public ResponseEntity<AuthResponse> unavailable(Exception e) {
        log.error("Password reset delivery unavailable", e);
        return ResponseEntity.status(503).body(AuthResponse.error("MAIL_UNAVAILABLE",
                "현재 재설정 메일을 보낼 수 없습니다. 잠시 후 다시 시도해 주세요.", Map.of()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> unexpected(Exception e) {
        log.error("Authentication request failed", e);
        return ResponseEntity.internalServerError().body(AuthResponse.error("SERVER_ERROR",
                "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", Map.of()));
    }
}
