package com.example.demoj2ee.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHome(HttpSession session, Model model) {
        // Kiểm tra xem có ai đang đăng nhập không
        Object user = session.getAttribute("loggedInUser");
        if (user != null) {
            model.addAttribute("message", "Chào mừng sếp đã quay lại Đế chế PELE Cinema!");
        } else {
            model.addAttribute("message", "Bạn chưa đăng nhập. Vui lòng vào /login");
        }
        return "home";
    }
}