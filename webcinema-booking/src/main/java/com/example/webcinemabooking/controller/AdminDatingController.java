package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.CinemaDateRepository;
import com.example.webcinemabooking.repository.MatchRequestRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminDatingController {

    @Autowired
    private CinemaDateRepository cinemaDateRepository;

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    // 1. GIAO DIỆN QUẢN LÝ TỔNG
    @GetMapping("/admin/dating")
    public String showAdminDating(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        // BẢO MẬT: Phải đăng nhập và Phải là ADMIN mới được vào
        // (Nếu lúc tạo acc sếp lưu role là kiểu khác thì nhớ sửa chữ "ADMIN" cho khớp nhé)
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khu vực cấm! Chỉ dành cho Ban Quản Trị rạp PELE.");
            return "redirect:/"; // Đuổi thẳng cổ về trang chủ
        }

        // Lấy toàn bộ bài đăng (kể cả cũ nhất) móc lên cho Admin soi
        model.addAttribute("allPosts", cinemaDateRepository.findAll());
        return "admin/admin-dating";
    }

    // 2. ADMIN TRẢM BÀI ĐĂNG (Bấm phát bay màu luôn cả bình luận bên trong)
    @PostMapping("/admin/dating/delete-post")
    public String deletePost(@RequestParam("postId") Long postId, RedirectAttributes redirectAttributes) {
        try {
            cinemaDateRepository.deleteById(postId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sổ bài đăng vi phạm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa bài: " + e.getMessage());
        }
        return "redirect:/admin/dating";
    }

    // 3. ADMIN TRẢM BÌNH LUẬN (Chỉ xóa bình luận xàm, giữ lại bài đăng gốc)
    @PostMapping("/admin/dating/delete-comment")
    public String deleteComment(@RequestParam("commentId") Long commentId, RedirectAttributes redirectAttributes) {
        try {
            matchRequestRepository.deleteById(commentId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã quét sạch bình luận rác!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa bình luận: " + e.getMessage());
        }
        return "redirect:/admin/dating";
    }
}