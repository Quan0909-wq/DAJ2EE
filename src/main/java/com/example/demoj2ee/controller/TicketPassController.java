package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.*;
import com.example.demoj2ee.repository.*;
import com.example.demoj2ee.service.TicketPassService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class TicketPassController {

    @Autowired private TicketPassService ticketPassService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private TicketPassRepository ticketPassRepository;

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }

    // ============================================================
    // TRANG CHO VE (DANH SACH VE DANG BAN)
    // ============================================================

    @GetMapping("/ticket-market")
    public String ticketMarket(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String filter,
                              HttpSession session,
                              Model model) {
        List<TicketPass> passes;

        if (keyword != null && !keyword.trim().isEmpty()) {
            passes = ticketPassService.searchPasses(keyword.trim());
        } else if ("my".equals(filter)) {
            User user = (User) session.getAttribute("loggedInUser");
            if (user != null) {
                passes = ticketPassService.getSellerPasses(user.getId());
            } else {
                passes = Collections.emptyList();
            }
        } else {
            passes = ticketPassService.getAvailablePasses();
        }

        User user = (User) session.getAttribute("loggedInUser");

        double totalPassPrice = passes.stream()
                .mapToDouble(TicketPass::getPassPrice)
                .sum();

        model.addAttribute("passes", passes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("filter", filter);
        model.addAttribute("currentUser", user);
        model.addAttribute("totalPassPrice", totalPassPrice);

        return "ticket-market";
    }

    // ============================================================
    // TRANG DANG VE DE PASS
    // ============================================================

    @GetMapping("/ticket/post")
    public String postTicketPage(HttpSession session,
                                 @RequestParam(required = false) Long bookingId,
                                 Model model,
                                 RedirectAttributes ra) {
        if (!isLoggedIn(session)) {
            ra.addFlashAttribute("redirectAfterLogin", "/ticket/post");
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("loggedInUser");
        List<Booking> availableBookings = ticketPassService.getBookingsAvailableForPass(user.getId());

        model.addAttribute("bookings", availableBookings);

        Long preselectId = bookingId;
        if (preselectId == null) {
            Object flashBid = model.getAttribute("bookingId");
            if (flashBid instanceof Long) {
                preselectId = (Long) flashBid;
            } else if (flashBid instanceof Number) {
                preselectId = ((Number) flashBid).longValue();
            }
        }
        model.addAttribute("preselectBookingId", preselectId != null ? preselectId.toString() : "");

        if (preselectId != null) {
            Booking selectedBooking = bookingRepository.findById(preselectId).orElse(null);
            model.addAttribute("selectedBooking", selectedBooking);
        }

        return "ticket-post";
    }

    @PostMapping("/ticket/post")
    public String postTicket(@RequestParam Long bookingId,
                            @RequestParam double passPrice,
                            @RequestParam(required = false) String reason,
                            @RequestParam(required = false) String contactInfo,
                            HttpSession session,
                            RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";

        User user = (User) session.getAttribute("loggedInUser");

        try {
            ticketPassService.createPass(bookingId, user.getId(), passPrice, reason, contactInfo);
            ra.addFlashAttribute("success", "Đăng vé thành công! Người khác có thể mua vé của bạn.");
            return "redirect:/ticket-market";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("bookingId", bookingId);
            return "redirect:/ticket/post";
        }
    }

    // ============================================================
    // CHI TIET VE PASS
    // ============================================================

    @GetMapping("/ticket/{id}")
    public String ticketDetail(@PathVariable Long id,
                              HttpSession session,
                              Model model,
                              RedirectAttributes ra) {
        TicketPass pass = ticketPassService.getPassById(id).orElse(null);
        if (pass == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé");
            return "redirect:/ticket-market";
        }

        User user = (User) session.getAttribute("loggedInUser");
        boolean canBuy = false;
        boolean canCancel = false;

        if (user != null) {
            canBuy = ticketPassService.canUserBuy(id, user.getId());
            canCancel = ticketPassService.canUserCancel(id, user.getId());
        }

        model.addAttribute("pass", pass);
        model.addAttribute("currentUser", user);
        model.addAttribute("canBuy", canBuy);
        model.addAttribute("canCancel", canCancel);

        return "ticket-detail";
    }

    // ============================================================
    // MUA VE (XAC NHAN PASS)
    // ============================================================

    @PostMapping("/ticket/{id}/buy")
    public String buyTicket(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes ra) {
        if (!isLoggedIn(session)) {
            ra.addFlashAttribute("redirectAfterLogin", "/ticket/" + id);
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("loggedInUser");

        try {
            TicketPass pass = ticketPassService.buyPass(id, user.getId());
            ra.addFlashAttribute("success", "Mua vé thành công! Bạn có thể xem vé trong mục 'Vé của tôi'.");
            return "redirect:/my-tickets";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ticket/" + id;
        }
    }

    // ============================================================
    // HUY VE DANG BAN
    // ============================================================

    @PostMapping("/ticket/{id}/cancel")
    public String cancelTicket(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";

        User user = (User) session.getAttribute("loggedInUser");

        try {
            ticketPassService.cancelPass(id, user.getId());
            ra.addFlashAttribute("success", "Đã hủy đăng vé thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/ticket-market";
    }

    // ============================================================
    // MY PASS LIST (VE CUA TOI DANG BAN)
    // ============================================================

    @GetMapping("/my-passes")
    public String myPasses(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isLoggedIn(session)) {
            ra.addFlashAttribute("redirectAfterLogin", "/my-passes");
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("loggedInUser");
        List<TicketPass> myPasses = ticketPassService.getSellerPasses(user.getId());

        model.addAttribute("passes", myPasses);
        return "my-passes";
    }
}
