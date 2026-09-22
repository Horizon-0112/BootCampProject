package com.xnaver.project.service;
import com.xnaver.project.component.PasswordCryptoComponent;
import com.xnaver.project.dto.MyPageDto.*;
import com.xnaver.project.entity.UserEntity;
import com.xnaver.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    private final UserRepository users;
    private final MyPageRepository activity;
    private final PasswordCryptoComponent crypto;
    private final ProfileImageRepository profileImages;
    private UserEntity user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "탈퇴했거나 유효하지 않은 계정입니다. 다시 로그인해 주세요."));
    }
    private UserEntity lockedUser(String email) {
        return users.findForUpdateByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 계정입니다."));
    }
    public Profile profile(String email) {
        var u = user(email);
        return new Profile(u.getEmail(), u.getNickName(), u.getCreatedAt(),
                activity.count("recipes", u.getIdx()), activity.count("comments", u.getIdx()), activity.count("bookmarks", u.getIdx()),
                profileImages.existsById(u.getIdx()));
    }
    public Page list(String email, String kind, int page, int size) {
        if (page < 0 || size < 1 || size > 50)
            throw new AuthValidationException(Map.of("page", "페이지는 0 이상, 조회 개수는 1~50으로 지정해 주세요."));
        return activity.list(kind, user(email).getIdx(), page, size);
    }
    private void verifyPassword(UserEntity user, String password) {
        if (password == null || password.isBlank() || !crypto.matches(password, user.getPassword()))
            throw new AuthValidationException(Map.of("currentPassword", "현재 비밀번호가 올바르지 않습니다."));
    }
    @Transactional
    public boolean update(String email, Update dto) {
        var user = lockedUser(email);
        verifyPassword(user, dto.currentPassword());
        var errors = new LinkedHashMap<String, String>();
        String nickname = dto.nickName() == null ? "" : dto.nickName().trim();
        if (nickname.isEmpty() || nickname.length() > 30) errors.put("nickName", "닉네임을 1~30자로 입력해 주세요.");
        else if (users.existsByNickNameAndIdxNot(nickname, user.getIdx())) errors.put("nickName", "이미 사용 중인 닉네임입니다.");
        boolean changePassword = dto.pw() != null && !dto.pw().isEmpty();
        if (changePassword || (dto.pwConfirm() != null && !dto.pwConfirm().isEmpty()))
            AuthValidation.password(dto.pw(), dto.pwConfirm(), errors);
        AuthValidation.check(errors);
        user.setNickName(nickname);
        if (changePassword) {
            user.setPassword(crypto.encrypt(dto.pw()));
            activity.removeResetTokens(user.getIdx());
        }
        users.flush();
        return changePassword;
    }
    @Transactional
    public void withdraw(String email, Withdrawal dto) {
        var user = lockedUser(email);
        verifyPassword(user, dto.currentPassword());
        if (!dto.confirmed()) throw new AuthValidationException(Map.of("confirmed", "탈퇴 시 삭제되는 정보를 확인하고 동의해 주세요."));
        activity.removeAccountData(user.getIdx());
        users.delete(user);
        users.flush();
    }
}
