package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Booking;
import com.example.demoj2ee.model.TicketDispute;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.BookingRepository;
import com.example.demoj2ee.repository.TicketDisputeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/disputes")
public class TicketDisputeController {

    @Autowired
    private TicketDisputeRepository ticketDisputeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    private boolean isLoggedIn(HttpSession session) {
        return getLoggedInUser(session) != null;
    }

    @GetMapping("/create/{bookingId}")
    public String showDisputeForm(@PathVariable Long bookingId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";

        User user = getLoggedInUser(session);
        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        if (booking == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé cần khiếu nại!");
            return "redirect:/";
        }

        if (booking.getUser() == null || !booking.getUser().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền khiếu nại vé này!");
            return "redirect:/";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("dispute", new TicketDispute());
        return "dispute-form";
    }

    @PostMapping("/create")
    public String createDispute(@RequestParam Long bookingId,
                                @RequestParam String subject,
                                @RequestParam String content,
                                HttpSession session,
                                RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";

        User user = getLoggedInUser(session);
        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        if (booking == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé cần khiếu nại!");
            return "redirect:/";
        }

        if (booking.getUser() == null || !booking.getUser().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền khiếu nại vé này!");
            return "redirect:/";
        }

        TicketDispute dispute = new TicketDispute();
        dispute.setUser(user);
        dispute.setBooking(booking);
        dispute.setSubject(subject);
        dispute.setContent(content);
        dispute.setStatus("PENDING");

        ticketDisputeRepository.save(dispute);

        ra.addFlashAttribute("success", "Gửi khiếu nại thành công!");
        return "redirect:/disputes/my";
    }

    @GetMapping("/my")
    public String myDisputes(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";

        User user = getLoggedInUser(session);
        List<TicketDispute> disputes = ticketDisputeRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        model.addAttribute("disputes", disputes);
        return "my-disputes";
    }
}