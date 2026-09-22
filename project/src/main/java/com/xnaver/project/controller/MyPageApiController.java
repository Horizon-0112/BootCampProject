package com.xnaver.project.controller;
import com.xnaver.project.dto.AuthResponse;
import com.xnaver.project.dto.MyPageDto.*;
import com.xnaver.project.service.MyPageService;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MyPageApiController {
    private final MyPageService service;
    private final com.xnaver.project.service.ProfileImageService profileImages;
    @PostMapping(value = "/profile-image", consumes = "multipart/form-data")
    public AuthResponse uploadImage(Authentication auth, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        profileImages.upload(auth.getName(), file);
        return AuthResponse.ok("프로필 사진이 등록되었습니다.", null);
    }
    @GetMapping("/profile-image")
    public org.springframework.http.ResponseEntity<byte[]> image(Authentication auth) {
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.IMAGE_PNG)
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .body(profileImages.read(auth.getName()));
    }
    @DeleteMapping("/profile-image")
    public AuthResponse deleteImage(Authentication auth) {
        profileImages.delete(auth.getName());
        return AuthResponse.ok("프로필 사진이 삭제되었습니다.", null);
    }
    private final org.springframework.security.core.session.SessionRegistry sessions;
    private void expireSessions(Authentication auth) {
        sessions.getAllPrincipals().stream()
                .filter(principal -> principal instanceof org.springframework.security.core.userdetails.UserDetails user
                        && user.getUsername().equals(auth.getName()))
                .forEach(principal -> sessions.getAllSessions(principal, false)
                        .forEach(org.springframework.security.core.session.SessionInformation::expireNow));
    }
    @GetMapping
    public Profile profile(Authentication auth) { return service.profile(auth.getName()); }
    @GetMapping("/recipes")
    public Page recipes(Authentication auth, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return service.list(auth.getName(), "recipes", page, size);
    }
    @GetMapping("/comments")
    public Page comments(Authentication auth, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return service.list(auth.getName(), "comments", page, size);
    }
    @GetMapping("/bookmarks")
    public Page bookmarks(Authentication auth, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return service.list(auth.getName(), "bookmarks", page, size);
    }
    @PatchMapping
    public AuthResponse update(Authentication auth, @RequestBody Update dto, HttpServletRequest request, HttpServletResponse response) {
        boolean changedPassword = service.update(auth.getName(), dto);
        if (changedPassword) {
            expireSessions(auth);
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return AuthResponse.ok(changedPassword ? "비밀번호가 변경되었습니다. 다시 로그인해 주세요." : "내 정보가 수정되었습니다.",
                changedPassword ? request.getContextPath() + "/auth/login?reset" : null);
    }
    @DeleteMapping
    public AuthResponse withdraw(Authentication auth, @RequestBody Withdrawal dto, HttpServletRequest request, HttpServletResponse response) {
        service.withdraw(auth.getName(), dto);
        expireSessions(auth);
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return AuthResponse.ok("회원 탈퇴가 완료되었습니다.", request.getContextPath() + "/auth/login?withdrawn");
    }
}
