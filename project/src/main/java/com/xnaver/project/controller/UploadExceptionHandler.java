package com.xnaver.project.controller;

import com.xnaver.project.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.Map;

// Multipart parsing can fail before Spring has selected a controller.
@RestControllerAdvice
public class UploadExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<AuthResponse> tooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(413).body(AuthResponse.error("FILE_TOO_LARGE",
                "사진은 5MB 이하로 선택해 주세요.", Map.of("file", "사진은 5MB 이하로 선택해 주세요.")));
    }
}
