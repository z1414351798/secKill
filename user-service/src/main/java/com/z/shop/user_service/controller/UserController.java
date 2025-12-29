package com.z.shop.user_service.controller;

import com.z.shop.user_service.domain.User;
import com.z.shop.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password) {
        return userService.login(username, password);
    }

    @PostMapping("/create")
    public void create(@RequestBody User user) {
        userService.createUser(user);
    }
}
