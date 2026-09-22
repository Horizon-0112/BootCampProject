package com.xnaver.project.controller;
import com.xnaver.project.dto.*;
import com.xnaver.project.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {
    private final UserSignUpService signUpService;
    private final PasswordResetService resetService;
    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@ModelAttribute UserSignUpDto dto, HttpServletRequest request) {
        signUpService.signUp(dto);
        return ResponseEntity.status(201).body(AuthResponse.ok("회원가입이 완료되었습니다.",
                request.getContextPath() + "/auth/login?signup"));
    }
    @PostMapping("/password/forgot")
    public AuthResponse forgot(@RequestParam(defaultValue = "") String email) {
        resetService.requestReset(email);
        return AuthResponse.ok("가입된 이메일이면 재설정 링크를 발송했습니다. 메일함을 확인해 주세요. 재발송은 1분 뒤 가능합니다.", null);
    }
    @PostMapping("/password/reset")
    public AuthResponse reset(@ModelAttribute UserPasswordResetDto dto, HttpServletRequest request) {
        resetService.reset(dto);
        return AuthResponse.ok("비밀번호가 변경되었습니다.", request.getContextPath() + "/auth/login?reset");
    }
}