package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Comment;
import com.example.demoj2ee.model.TicketPass;
import com.example.demoj2ee.model.Booking;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.*;
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
@RequestMapping("/admin")
public class AdminController {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private TicketPassRepository ticketPassRepository;

    private static List<Map<String, Object>> danhSachMa = new ArrayList<>();

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping({"", "/"})
    public String dashboard(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        Double totalRevenue = bookingRepository.sumTotalAmount();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("totalTickets", bookingRepository.countTotalBookings());
        model.addAttribute("totalMovies", movieRepository.count());
        model.addAttribute("buyingCustomers", bookingRepository.countDistinctCustomers());
        model.addAttribute("registeredUsers", userRepository.countByRole("USER"));
        model.addAttribute("adminName", ((User) session.getAttribute("loggedInUser")).getFullName());

        return "admin/dashboard";
    }

    // ================= QUAN LY TRANH CHAP CHO VE =================

    @GetMapping("/ticket-disputes")
    public String viewDisputes(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        List<TicketPass> disputedTickets = ticketPassRepository.findByStatusOrderByCreatedAtDesc("DISPUTED");
        model.addAttribute("disputedTickets", disputedTickets);
        return "redirect:/admin";
    }

    @PostMapping("/ticket-disputes/resolve")
    public String resolveDispute(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes ra) {
        if (!checkAdmin(session)) return "redirect:/";
        TicketPass tp = ticketPassRepository.findById(passId).orElse(null);
        if (tp != null) {
            tp.setStatus("SOLD");
            ticketPassRepository.save(tp);
            Booking b = tp.getBooking();
            b.setCustomerName(tp.getBuyer().getUsername() + " (Boss Quan xu)");
            b.setCustomerEmail(tp.getBuyer().getEmail());
            bookingRepository.save(b);
            ra.addFlashAttribute("successMessage", "Da cuong che sang ten ve cho nguoi mua thanh cong!");
        }
        return "redirect:/admin/ticket-disputes";
    }

    @PostMapping("/ticket-disputes/reject")
    public String rejectDispute(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes ra) {
        if (!checkAdmin(session)) return "redirect:/";
        TicketPass tp = ticketPassRepository.findById(passId).orElse(null);
        if (tp != null) {
            tp.setStatus("AVAILABLE");
            tp.setBuyer(null);
            ticketPassRepository.save(tp);
            ra.addFlashAttribute("successMessage", "Da huy bao cao lua dao. Ve da duoc tra lai cho!");
        }
        return "redirect:/admin/ticket-disputes";
    }

    // ================= QUAN LY MA GIAM GIA =================

    @GetMapping("/promotions")
    public String adminPromotions(Model model) {
        model.addAttribute("promotions", danhSachMa);
        return "redirect:/admin";
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

    // ================= QUAN LY BINH LUAN =================

    @GetMapping("/comments")
    public String manageComments(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        List<Comment> allComments = commentRepository.findAll();
        model.addAttribute("comments", allComments);
        return "redirect:/admin";
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
