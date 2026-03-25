package com.z.zPay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mock")
public class PageController {

    @GetMapping("/pay-page")
    public String payPage(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "pay-page";
    }
}