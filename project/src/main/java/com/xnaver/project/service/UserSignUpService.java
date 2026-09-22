package com.xnaver.project.service;
import com.xnaver.project.component.PasswordCryptoComponent;
import com.xnaver.project.dto.UserSignUpDto;
import com.xnaver.project.entity.UserEntity;
import com.xnaver.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
@Service
@RequiredArgsConstructor
public class UserSignUpService {
    private final UserRepository userRepository;
    private final PasswordCryptoComponent passwordCryptoComponent;
    @Transactional
    public void signUp(UserSignUpDto dto) {
        var errors = new LinkedHashMap<String, String>();
        String email = AuthValidation.email(dto.getEmail(), errors);
        AuthValidation.password(dto.getPw(), dto.getPwConfirm(), errors);
        String nickname = dto.getNickName() == null ? "" : dto.getNickName().trim();
        if (nickname.isEmpty() || nickname.length() > 30)
            errors.put("nickName", "닉네임을 1~30자로 입력해 주세요.");
        AuthValidation.check(errors);
        if (userRepository.existsByEmail(email)) {
            errors.put("email", "이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByNickName(nickname)) {
            errors.put("nickName", "이미 사용 중인 닉네임입니다.");
        }
        AuthValidation.check(errors);
        userRepository.saveAndFlush(UserEntity.builder().email(email)
                .password(passwordCryptoComponent.encrypt(dto.getPw())).nickName(nickname).build());
    }
}
