package com.example.demoj2ee.controller; // Nhớ đổi tên package theo project sếp nha

import com.example.demoj2ee.model.User;
import com.example.demoj2ee.service.AuthService;import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    // Mở giao diện
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // XỬ LÝ KHI BẤM NÚT ĐĂNG KÝ
    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        String result = authService.registerNewUser(user);

        if ("SUCCESS".equals(result)) {
            // Gửi tin nhắn thành công qua HTML
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Hãy đăng nhập để vào Rạp PELE.");
        } else {
            // Gửi tin nhắn lỗi (trùng username)
            redirectAttributes.addFlashAttribute("error", result);
        }
        return "redirect:/login"; // Đá về lại trang login
    }

    // XỬ LÝ KHI BẤM NÚT ĐĂNG NHẬP
    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        User user = authService.authenticate(username, password);

        if (user != null) {
            // ĐĂNG NHẬP THÀNH CÔNG: Lưu thông tin vào Session (Phiên làm việc)
            session.setAttribute("loggedInUser", user);
            return "redirect:/"; // Đá về trang chủ
        } else {
            // THẤT BẠI: Báo lỗi
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            return "redirect:/login";
        }
    }

    // XỬ LÝ ĐĂNG XUẤT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa sạch trí nhớ của hệ thống về user này
        return "redirect:/login";
    }
}