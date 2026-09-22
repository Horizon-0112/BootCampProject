package com.xnaver.project.service;
import com.xnaver.project.component.PasswordCryptoComponent;
import com.xnaver.project.entity.UserEntity;
import com.xnaver.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class UserLoginService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordCryptoComponent passwordCryptoComponent;
    private final String dummyHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("dummy-password");
    public UserDetails login(String email, String plainPassword) {
        String normalized = email == null ? "" : email.trim();
        UserEntity user = userRepository.findByEmail(normalized).orElse(null);
        boolean matches = passwordCryptoComponent.matches(plainPassword == null ? "" : plainPassword,
                user == null ? dummyHash : user.getPassword());
        if (user == null || !matches)
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        return toUserDetails(user);
    }
    @Override
    public UserDetails loadUserByUsername(String email) {
        return toUserDetails(userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다.")));
    }
    private UserDetails toUserDetails(UserEntity user) {
        return User.withUsername(user.getEmail()).password(user.getPassword()).roles("USER").build();
    }
}