package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Booking;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String listBookings(
            HttpSession session, Model model,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "date", required = false) String date) {
        if (!checkAdmin(session)) return "redirect:/";

        List<Booking> bookings;
        Double totalRevenue = bookingRepository.sumTotalAmount() != null ? bookingRepository.sumTotalAmount() : 0.0;

        if (search != null && !search.isBlank()) {
            bookings = bookingRepository.findByCustomerNameContainingIgnoreCase(search);
        } else if (status != null && !status.equals("all")) {
            bookings = bookingRepository.findByStatus(status);
        } else {
            bookings = bookingRepository.findAll();
        }

        double filteredRevenue = bookings.stream()
                .filter(b -> "SUCCESS".equals(b.getStatus()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        model.addAttribute("bookings", bookings);
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDate", date);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("filteredRevenue", filteredRevenue);
        model.addAttribute("totalBookings", bookingRepository.countTotalBookings());
        return "admin/quan-ly-hoa-don";
    }

    @GetMapping("/detail/{id}")
    public String bookingDetail(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return "redirect:/admin/bookings";
        model.addAttribute("booking", booking);
        return "admin/booking-detail";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("id") Long id, @RequestParam("status") String status, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setStatus(status);
            bookingRepository.save(booking);
        }
        return "redirect:/admin/bookings";
    }
}
