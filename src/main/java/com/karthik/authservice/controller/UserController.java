package com.karthik.authservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public String getProfile() {
        return "This is a protected API";
    }
}