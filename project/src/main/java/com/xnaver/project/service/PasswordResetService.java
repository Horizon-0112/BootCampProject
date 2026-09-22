package com.xnaver.project.service;
import com.xnaver.project.component.PasswordCryptoComponent;
import com.xnaver.project.dto.UserPasswordResetDto;
import com.xnaver.project.entity.PasswordResetToken;
import com.xnaver.project.repository.PasswordResetTokenRepository;
import com.xnaver.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordCryptoComponent crypto;
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final SecureRandom random = new SecureRandom();
    @Value("${app.auth.base-url:http://localhost:8080}")
    private String baseUrl;
    @Value("${app.auth.mail-from:no-reply@example.com}")
    private String mailFrom;

    @Transactional
    public void requestReset(String input) {
        var errors = new LinkedHashMap<String, String>();
        String email = AuthValidation.email(input, errors);
        AuthValidation.check(errors);
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) throw new IllegalStateException("메일 발송 설정이 필요합니다.");
        var user = users.findForUpdateByEmail(email);
        if (user.isEmpty()) return;
        Instant now = Instant.now();
        var previous = tokens.findByUserIdx(user.get().getIdx());
        if (previous.isPresent()) {
            if (previous.get().getIssuedAt().plusSeconds(60).isAfter(now)) return;
            tokens.delete(previous.get());
            tokens.flush();
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.saveAndFlush(new PasswordResetToken(hash(token), user.get(), now));
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(user.get().getEmail());
        mail.setSubject("[자취생요리] 비밀번호 재설정");
        mail.setText("아래 링크에서 30분 이내에 비밀번호를 재설정해 주세요. 링크는 한 번만 사용할 수 있습니다.\n\n"
                + baseUrl.replaceAll("/+$", "") + "/auth/password/reset#token=" + token
                + "\n\n요청하지 않았다면 이 메일을 무시해 주세요.");
        sender.send(mail);
    }

    @Transactional
    public void reset(UserPasswordResetDto dto) {
        var errors = new LinkedHashMap<String, String>();
        AuthValidation.password(dto.getPw(), dto.getPwConfirm(), errors);
        AuthValidation.check(errors);
        if (dto.getToken() == null || !dto.getToken().matches("[A-Za-z0-9_-]{43}"))
            throw invalidToken();
        String hash = hash(dto.getToken());
        var candidate = tokens.findById(hash).orElseThrow(this::invalidToken);
        // Issuance and consumption both lock the user, serializing concurrent resets.
        var user = users.findForUpdateByEmail(candidate.getUser().getEmail()).orElseThrow(this::invalidToken);
        var current = tokens.findByUserIdx(user.getIdx()).orElseThrow(this::invalidToken);
        if (!current.getTokenHash().equals(hash) || !current.getExpiresAt().isAfter(Instant.now()))
            throw invalidToken();
        user.setPassword(crypto.encrypt(dto.getPw()));
        tokens.delete(current);
        tokens.flush();
    }

    private AuthValidationException invalidToken() {
        return new AuthValidationException(Map.of("token", "재설정 링크가 유효하지 않거나 만료되었습니다. 비밀번호 찾기를 다시 진행해 주세요."));
    }
    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}