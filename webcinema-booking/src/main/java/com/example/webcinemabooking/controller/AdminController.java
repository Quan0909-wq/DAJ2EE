package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Comment;
import com.example.webcinemabooking.model.TicketPass; // Thêm import này
import com.example.webcinemabooking.model.Booking;    // Thêm import này
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.*;    // Import hết repo cho gọn
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Autowired private CommentRepository commentRepository;

    // --- TIÊM THÊM: Repository Chợ vé để sếp làm Quan Tòa ---
    @Autowired private TicketPassRepository ticketPassRepository;

    // --- KHO CHỨA MÃ GIẢM GIÁ ---
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

    // ================= [MỚI] QUẢN LÝ TRANH CHẤP CHỢ VÉ =================

    // 1. Trang danh sách các vụ kiện (DISPUTED)
    @GetMapping("/ticket-disputes")
    public String viewDisputes(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        // Lấy những vé đang bị gắn mác TRANH CHẤP
        List<TicketPass> disputedTickets = ticketPassRepository.findByStatusOrderByCreatedAtDesc("DISPUTED");
        model.addAttribute("disputedTickets", disputedTickets);

        return "admin/ticket-disputes"; // Sếp nhớ tạo file này trong templates/admin/ nhé
    }

    // 2. Admin xử NGƯỜI MUA thắng -> Cưỡng chế sang tên vé
    @PostMapping("/ticket-disputes/resolve")
    public String resolveDispute(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes ra) {
        if (!checkAdmin(session)) return "redirect:/";

        TicketPass tp = ticketPassRepository.findById(passId).orElse(null);
        if (tp != null) {
            // Đánh dấu đã bán xong
            tp.setStatus("SOLD");
            ticketPassRepository.save(tp);

            // Cưỡng chế đổi thông tin trên Booking (Sang tên chính thức)
            Booking b = tp.getBooking();
            b.setCustomerName(tp.getBuyer().getUsername() + " (Boss Quan xử)");
            b.setCustomerEmail(tp.getBuyer().getEmail());
            bookingRepository.save(b);

            ra.addFlashAttribute("successMessage", "⚖️ Đã cưỡng chế sang tên vé cho người mua thành công!");
        }
        return "redirect:/admin/ticket-disputes";
    }

    // 3. Admin xử NGƯỜI BÁN thắng -> Hủy báo cáo, trả vé về trạng thái cũ
    @PostMapping("/ticket-disputes/reject")
    public String rejectDispute(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes ra) {
        if (!checkAdmin(session)) return "redirect:/";

        TicketPass tp = ticketPassRepository.findById(passId).orElse(null);
        if (tp != null) {
            // Nhả vé lại ra chợ (AVAILABLE) và đuổi thằng mua ra khỏi vé này
            tp.setStatus("AVAILABLE");
            tp.setBuyer(null);
            ticketPassRepository.save(tp);

            ra.addFlashAttribute("successMessage", "⚖️ Đã hủy báo cáo lừa đảo. Vé đã được trả lại chợ!");
        }
        return "redirect:/admin/ticket-disputes";
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
        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotions/delete/{code}")
    public String deletePromotion(@PathVariable("code") String code) {
        danhSachMa.removeIf(promo -> promo.get("code").toString().equals(code));
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
                break;
            }
        }
        return "redirect:/admin/promotions";
    }

    // ================= QUẢN LÝ BÌNH LUẬN =================

    @GetMapping("/comments")
    public String manageComments(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        List<Comment> allComments = commentRepository.findAll();
        model.addAttribute("comments", allComments);
        return "admin/admin-comments";
    }

    @GetMapping("/comments/delete/{id}")
    public String deleteComment(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";
        commentRepository.deleteById(id);
        return "redirect:/admin/comments";
    }

    @PostMapping("/comments/edit")
    public String editComment(@RequestParam("id") Long id, @RequestParam("content") String content, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setContent(content);
            commentRepository.save(comment);
        }
        return "redirect:/admin/comments";
    }
}