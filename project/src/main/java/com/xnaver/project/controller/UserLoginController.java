package com.xnaver.project.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class UserLoginController {
    @GetMapping("/")
    public String index() { return "pages/index"; }
    @GetMapping("/fridge")
    public String fridge() { return "pages/fridge"; }
    @GetMapping("/auth/login")
    public String login() { return "auth/login"; }
    @GetMapping("/auth/password/forgot")
    public String forgotPassword() { return "auth/password-forgot"; }
    @GetMapping("/auth/password/reset")
    public String resetPassword() { return "auth/password-reset"; }
    @GetMapping("/login")
    public String legacyLogin() { return "redirect:/auth/login"; }
    @GetMapping("/password-reset")
    public String legacyReset() { return "redirect:/auth/password/forgot"; }
}