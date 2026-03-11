package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Comment;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.BookingRepository;
import com.example.webcinemabooking.repository.CommentRepository;
import com.example.webcinemabooking.repository.MovieRepository;
import com.example.webcinemabooking.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin") // Mọi đường dẫn trong này đều bắt đầu bằng /admin
public class AdminController {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private UserRepository userRepository;

    // --- MỚI THÊM: Tiêm Repository Bình luận để sếp kiểm duyệt ---
    @Autowired private CommentRepository commentRepository;

    // --- KHO CHỨA MÃ GIẢM GIÁ (Tạm thời lưu trên RAM để test) ---
    private static List<Map<String, Object>> danhSachMa = new ArrayList<>();

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping({"", "/"})
    public String dashboard(HttpSession session, Model model) {
        if (!checkAdmin(session)) {
            return "redirect:/";
        }

        Double totalRevenue = bookingRepository.sumTotalAmount();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("totalTickets", bookingRepository.countTotalBookings());
        model.addAttribute("totalMovies", movieRepository.count());
        model.addAttribute("buyingCustomers", bookingRepository.countDistinctCustomers());
        model.addAttribute("registeredUsers", userRepository.countByRole("USER"));
        model.addAttribute("adminName", ((User) session.getAttribute("loggedInUser")).getFullName());

        return "admin/dashboard";
    }

    // ================= QUẢN LÝ MÃ GIẢM GIÁ =================

    @GetMapping("/promotions")
    public String adminPromotions(Model model) {
        model.addAttribute("promotions", danhSachMa);
        return "admin/admin-promotions";
    }

    @PostMapping("/promotions/add")
    public String addPromotion(@RequestParam("code") String code,
                               @RequestParam("discountAmount") int discountAmount,
                               @RequestParam("expiryDate") String expiryDate) {

        Map<String, Object> maMoi = new HashMap<>();
        maMoi.put("code", code);
        maMoi.put("discountAmount", discountAmount);
        maMoi.put("expiryDate", expiryDate);

        danhSachMa.add(maMoi);
        System.out.println("🎉 Sếp Quan vừa tạo mã: " + code + " | Giảm: " + discountAmount);

        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotions/delete/{code}")
    public String deletePromotion(@PathVariable("code") String code) {
        danhSachMa.removeIf(promo -> promo.get("code").toString().equals(code));
        System.out.println("🗑️ Sếp vừa cho mã " + code + " bay màu!");
        return "redirect:/admin/promotions";
    }

    @PostMapping("/promotions/edit")
    public String editPromotion(@RequestParam("code") String code,
                                @RequestParam("discountAmount") int discountAmount,
                                @RequestParam("expiryDate") String expiryDate) {
        for (Map<String, Object> promo : danhSachMa) {
            if (promo.get("code").toString().equals(code)) {
                promo.put("discountAmount", discountAmount);
                promo.put("expiryDate", expiryDate);
                System.out.println("✍️ Sếp vừa sửa mã " + code);
                break;
            }
        }
        return "redirect:/admin/promotions";
    }

    // ================= QUẢN LÝ BÌNH LUẬN (MỚI THÊM) =================

    // 1. Xem toàn bộ bình luận của rạp
    @GetMapping("/comments")
    public String manageComments(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        // Lấy hết bình luận ra cho sếp soi, cái nào mới đăng thì hiện lên đầu
        List<Comment> allComments = commentRepository.findAll();
        model.addAttribute("comments", allComments);

        return "admin/admin-comments"; // Đường dẫn tới file HTML quản lý
    }

    // 2. Xóa bình luận nếu thấy không hợp lý (Thanh trừng)
    @GetMapping("/comments/delete/{id}")
    public String deleteComment(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";

        commentRepository.deleteById(id);
        System.out.println("🗑️ Sếp Quan vừa xóa sạch bình luận ID: " + id);

        return "redirect:/admin/comments";
    }

    // 3. Sửa bình luận (Nếu sếp muốn "nhẹ tay" sửa lại nội dung cho khách)
    @PostMapping("/comments/edit")
    public String editComment(@RequestParam("id") Long id, @RequestParam("content") String content, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";

        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setContent(content);
            commentRepository.save(comment);
            System.out.println("✍️ Sếp Quan vừa chỉnh sửa lại nội dung bình luận ID: " + id);
        }

        return "redirect:/admin/comments";
    }
}