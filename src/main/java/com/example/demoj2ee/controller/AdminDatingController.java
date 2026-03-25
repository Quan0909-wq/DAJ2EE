package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.CinemaDateRepository;
import com.example.demoj2ee.repository.MatchRequestRepository;
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

    @GetMapping("/admin/dating")
    public String showAdminDating(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khu vuc cam! Chi danh cho Ban Quan Tri.");
            return "redirect:/";
        }

        model.addAttribute("allPosts", cinemaDateRepository.findAll());
        return "admin/admin-dating";
    }

    @PostMapping("/admin/dating/delete-post")
    public String deletePost(@RequestParam("postId") Long postId, RedirectAttributes redirectAttributes) {
        try {
            cinemaDateRepository.deleteById(postId);
            redirectAttributes.addFlashAttribute("successMessage", "Da xoa bai dang vi pham!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loi khi xoa bai: " + e.getMessage());
        }
        return "redirect:/admin/dating";
    }

    @PostMapping("/admin/dating/delete-comment")
    public String deleteComment(@RequestParam("commentId") Long commentId, RedirectAttributes redirectAttributes) {
        try {
            matchRequestRepository.deleteById(commentId);
            redirectAttributes.addFlashAttribute("successMessage", "Da quet sach binh luan rac!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loi khi xoa binh luan: " + e.getMessage());
        }
        return "redirect:/admin/dating";
    }
}
