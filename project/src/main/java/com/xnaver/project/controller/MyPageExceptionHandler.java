package com.xnaver.project.controller;
import com.xnaver.project.dto.AuthResponse;
import com.xnaver.project.service.AuthValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestControllerAdvice(assignableTypes = MyPageApiController.class)
public class MyPageExceptionHandler {
    @ExceptionHandler({org.springframework.web.multipart.support.MissingServletRequestPartException.class, org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<AuthResponse> missingFile(Exception e) {
        return ResponseEntity.badRequest().body(AuthResponse.error("INVALID_REQUEST", "사진을 선택해 주세요.", Map.of("file", "사진 파일이 필요합니다.")));
    }
    @ExceptionHandler(AuthValidationException.class)
    public ResponseEntity<AuthResponse> validation(AuthValidationException e) {
        return ResponseEntity.badRequest().body(AuthResponse.error("VALIDATION_ERROR", e.getMessage(), e.getFieldErrors()));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<AuthResponse> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(AuthResponse.error("REQUEST_FAILED", e.getReason(), Map.of()));
    }
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<AuthResponse> conflict(Exception e) {
        return ResponseEntity.status(409).body(AuthResponse.error("CONFLICT", "다른 요청과 충돌했습니다. 닉네임 중복 여부를 확인하고 다시 시도해 주세요.", Map.of()));
    }
    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class, org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<AuthResponse> malformed(Exception e) {
        return ResponseEntity.badRequest().body(AuthResponse.error("INVALID_REQUEST", "요청 형식을 확인해 주세요.", Map.of()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> unexpected(Exception e) {
        org.slf4j.LoggerFactory.getLogger(getClass()).error("My page request failed", e);
        return ResponseEntity.internalServerError().body(AuthResponse.error("SERVER_ERROR", "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", Map.of()));
    }
}
