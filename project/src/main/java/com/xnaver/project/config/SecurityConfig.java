package com.xnaver.project.config;
import com.xnaver.project.component.UserAuthenticationProvider;
import com.xnaver.project.dto.AuthResponse;
import com.xnaver.project.service.AuthValidation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.*;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
@Configuration
public class SecurityConfig {
    @Bean
    org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }
    @Bean
    org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }
    private void json(HttpServletResponse response, ObjectMapper mapper, int status, AuthResponse body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(body));
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserAuthenticationProvider provider,
                                            ObjectMapper mapper, org.springframework.security.core.session.SessionRegistry sessions) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/auth/**", "/login", "/sign-up", "/password-reset",
                        "/api/v1/auth/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                .anyRequest().authenticated())
            .authenticationProvider(provider)
            .sessionManagement(session -> session.maximumSessions(-1).sessionRegistry(sessions)
                .expiredSessionStrategy(event -> {
                    var request = event.getRequest();
                    if (request.getRequestURI().startsWith(request.getContextPath() + "/api/"))
                        json(event.getResponse(), mapper, 401, AuthResponse.error("SESSION_EXPIRED", "세션이 만료되었습니다. 다시 로그인해 주세요.", Map.of()));
                    else event.getResponse().sendRedirect(request.getContextPath() + "/auth/login");
                }))
            .formLogin(form -> form.loginPage("/auth/login")
                .loginProcessingUrl("/api/v1/auth/login")
                .usernameParameter("email").passwordParameter("pw")
                .successHandler((request, response, authentication) ->
                    json(response, mapper, 200, AuthResponse.ok("로그인되었습니다.", request.getContextPath() + "/")))
                .failureHandler((request, response, exception) -> {
                    var errors = new LinkedHashMap<String, String>();
                    AuthValidation.email(request.getParameter("email"), errors);
                    if (request.getParameter("pw") == null || request.getParameter("pw").isBlank())
                        errors.put("pw", "비밀번호를 입력해 주세요.");
                    String code = "BAD_CREDENTIALS";
                    String message = "이메일 또는 비밀번호가 올바르지 않습니다. 다시 확인해 주세요.";
                    int status = 401;
                    if (!errors.isEmpty()) {
                        code = "VALIDATION_ERROR"; message = "입력 내용을 확인해 주세요."; status = 400;
                    } else if (exception instanceof LockedException) {
                        code = "ACCOUNT_LOCKED"; message = "잠긴 계정입니다. 관리자에게 문의해 주세요.";
                    } else if (exception instanceof DisabledException) {
                        code = "ACCOUNT_DISABLED"; message = "사용할 수 없는 계정입니다. 관리자에게 문의해 주세요.";
                    } else if (exception instanceof AuthenticationServiceException) {
                        code = "SERVER_ERROR"; message = "로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."; status = 503;
                    }
                    json(response, mapper, status, AuthResponse.error(code, message, errors));
                }).permitAll())
            .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                .logoutSuccessHandler((request, response, authentication) ->
                    json(response, mapper, 200, AuthResponse.ok("로그아웃되었습니다.", request.getContextPath() + "/auth/login?logout")))
                .invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) -> {
                    if (request.getRequestURI().startsWith(request.getContextPath() + "/api/"))
                        json(response, mapper, 401, AuthResponse.error("UNAUTHENTICATED", "로그인이 필요합니다. 다시 로그인해 주세요.", Map.of()));
                    else response.sendRedirect(request.getContextPath() + "/auth/login");
                })
                .accessDeniedHandler((request, response, exception) -> {
                    if (request.getRequestURI().startsWith(request.getContextPath() + "/api/"))
                        json(response, mapper, 403, AuthResponse.error("ACCESS_DENIED",
                            "요청 권한이 없거나 세션이 만료되었습니다. 페이지를 새로고침한 뒤 다시 시도해 주세요.", Map.of()));
                    else response.sendError(403);
                }));
        return http.build();
    }
}
