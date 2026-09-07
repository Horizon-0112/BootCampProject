package com.xnaver.project.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class TestController {

    @GetMapping
    public String index() {
        return "pages/index";
    }

    @GetMapping(path = "/fridge")
    public String fridge() {
        return "pages/fridge";
    }

    @GetMapping(path = "/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping(path = "/my-page")
    public String myPage() {
        return "pages/my-page";
    }

    @GetMapping(path = "/recipe-form")
    public String recipeForm() {
        return "pages/recipe-form";
    }

    @GetMapping(path = "/create-account")
    public String signup() {
        return "auth/create-account";
    }

    @GetMapping(path = "/shorts")
    public String shorts() {
        return "pages/shorts";
    }
}
