package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.UserRepository;
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
                               @RequestParam String phone, Model model) {
        // Kiểm tra xem tên đăng nhập hoặc email đã tồn tại chưa
        if (userRepository.findByUsername(username) != null || userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc Email đã được sử dụng!");
            return "register";
        }

        // Tạo tài khoản mới
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // Tạm thời lưu thẳng pass, sau này mình mã hóa sau nhé
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setPhone(phone);
        newUser.setRole("USER"); // Mặc định là khách hàng
        userRepository.save(newUser);

        return "redirect:/login?success=true"; // Đăng ký xong đẩy qua trang đăng nhập
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
            session.setAttribute("loggedInUser", user); // Lưu thông tin người dùng vào Session
            return "redirect:/"; // Đăng nhập thành công thì về Trang chủ
        }

        model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
        return "login";
    }

    // ----- PHẦN ĐĂNG XUẤT -----
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa Session
        return "redirect:/";
    }
}