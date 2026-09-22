package com.xnaver.project.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class MyPageController {
    @GetMapping("/my-page")
    public String page() { return "pages/my-page"; }
}
