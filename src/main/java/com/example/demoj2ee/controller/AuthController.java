package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // ----- PHẦN ĐĂNG KÝ -----
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password,
                               @RequestParam String email, @RequestParam String fullName,
                               @RequestParam String phone,
                               Model model) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Tên đăng nhập hoặc Email đã được sử dụng!");
            return "register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setPhone(phone);
        newUser.setActive(true); // tài khoản mới mặc định hoạt động
        userRepository.save(newUser);

        return "redirect:/login?success=true";
    }

    // ----- PHẦN ĐĂNG NHẬP -----
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password,
                            HttpSession session, Model model) {
        User user = userRepository.findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
            return "login";
        }

        if (!user.isActive()) {
            model.addAttribute("error", "Tài khoản của bạn đã bị khóa!");
            return "login";
        }

        session.setAttribute("loggedInUser", user);
        return "redirect:/";
    }

    // ----- PHẦN ĐĂNG XUẤT -----
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}