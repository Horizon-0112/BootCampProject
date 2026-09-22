package com.xnaver.project.component;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 해시를 담당하는 컴포넌트
 * BCrypt는 단방향 해시
 * 입력값과 DB 해시값을 matches로 비교
 */
@Component
public class PasswordCryptoComponent {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 평문 비밀번호를 BCrypt 해시로 변환합니다.
     */
    public String encrypt(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    /**
     * 비밀번호와 DB에 저장된 BCrypt로 해시 검사
     */
    public boolean matches(String plainPassword, String encodedPassword) {
        return encoder.matches(plainPassword, encodedPassword);
    }
}
