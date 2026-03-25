package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Booking;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    @Autowired
    private BookingRepository bookingRepository;

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping
    public String listBookings(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        // Lấy danh sách toàn bộ hóa đơn từ Database
        List<Booking> bookings = bookingRepository.findAll();
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }
}