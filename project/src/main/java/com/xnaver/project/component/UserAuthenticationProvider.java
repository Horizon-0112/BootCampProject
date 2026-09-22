package com.xnaver.project.component;

import com.xnaver.project.service.UserLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Spring Security 로그인 요청
 */
@Component
@RequiredArgsConstructor
public class UserAuthenticationProvider implements AuthenticationProvider {

    private final UserLoginService userLoginService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String pw = String.valueOf(authentication.getCredentials());

        try {
            UserDetails userDetails = userLoginService.login(email, pw);
            return UsernamePasswordAuthenticationToken.authenticated(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        } catch (org.springframework.dao.DataAccessException e) {
            throw new org.springframework.security.authentication.AuthenticationServiceException("로그인 처리 중 오류가 발생했습니다.", e);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
