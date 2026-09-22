package com.xnaver.project.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class UserSignUpController {
    @GetMapping("/auth/sign-up")
    public String signUp() { return "auth/sign-up"; }
    @GetMapping("/sign-up")
    public String legacySignUp() { return "redirect:/auth/sign-up"; }
}