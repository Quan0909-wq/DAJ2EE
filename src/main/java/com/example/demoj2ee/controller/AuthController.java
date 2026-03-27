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
        // Kiểm tra xem tên đăng nhập hoặc email đã tồn tại chưa
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Tên đăng nhập hoặc Email đã được sử dụng!");
            return "register";
        }

        // Tạo tài khoản mới (Đã bỏ thuộc tính Phone và Role để khớp Database)
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // Tạm thời lưu thẳng pass
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setPhone(phone);
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

        // Kiểm tra đúng tên và mật khẩu
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/";
        }

        model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
        return "login";
    }

    // ----- PHẦN ĐĂNG XUẤT -----
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}