package com.xnaver.project.service;
import java.util.Map;
import java.util.regex.Pattern;
public final class AuthValidation {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])[^\\s]{8,20}$");
    private AuthValidation() {}
    public static String email(String value, Map<String, String> errors) {
        String email = value == null ? "" : value.trim();
        if (email.length() > 50 || !EMAIL.matcher(email).matches())
            errors.put("email", "올바른 이메일을 50자 이내로 입력해 주세요.");
        return email;
    }
    public static void password(String pw, String confirm, Map<String, String> errors) {
        if (pw == null || !PASSWORD.matcher(pw).matches())
            errors.put("pw", "비밀번호는 공백 없이 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다.");
        if (confirm == null || confirm.isEmpty() || !confirm.equals(pw))
            errors.put("pwConfirm", "비밀번호가 일치하지 않습니다.");
    }
    public static void check(Map<String, String> errors) {
        if (!errors.isEmpty()) throw new AuthValidationException(errors);
    }
}