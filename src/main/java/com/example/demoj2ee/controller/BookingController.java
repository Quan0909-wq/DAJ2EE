package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Booking;
import com.example.demoj2ee.model.Showtime;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.BookingRepository;
import com.example.demoj2ee.repository.ProductRepository;
import com.example.demoj2ee.repository.ShowtimeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class BookingController {

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ProductRepository productRepository;

    // 1. Hiển thị sơ đồ ghế
    @GetMapping("/booking/{id}")
    public String showSeatMap(@PathVariable("id") Long id, Model model) {
        Showtime showtime = showtimeRepository.findById(id).orElse(null);
        if (showtime == null) return "redirect:/";

        model.addAttribute("showtime", showtime);
        model.addAttribute("rows", 10); // Fix cứng số hàng ghế để test trước
        model.addAttribute("cols", 10);
        model.addAttribute("products", productRepository.findAll());

        return "booking";
    }

    // 2. Chuyển sang trang nhập thông tin
    @PostMapping("/booking/checkout")
    public String showCheckoutPage(@RequestParam Long showtimeId,
                                   @RequestParam String seats,
                                   @RequestParam double totalAmount,
                                   HttpSession session,
                                   Model model) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", loggedInUser);
        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);

        // Tự động áp mã giảm giá 20% trên tổng bill để tiết kiệm chi phí
        double discountedAmount = totalAmount * 0.8;
        model.addAttribute("totalAmount", discountedAmount);

        return "checkout";
    }

    // 3. XÁC NHẬN VÀ LƯU VÉ
    @PostMapping("/booking/confirm")
    public String confirmBooking(@RequestParam Long showtimeId,
                                 @RequestParam String seats,
                                 @RequestParam(defaultValue = "0") double foodAmount,
                                 @RequestParam String customerName,
                                 @RequestParam String customerEmail,
                                 @RequestParam double finalAmount, // Nhận giá đã giảm từ form
                                 HttpSession session,
                                 RedirectAttributes ra) {

        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumbers(seats);
        booking.setTotalAmount(finalAmount);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("SUCCESS");
        if (loggedInUser != null) booking.setUser(loggedInUser);

        Booking savedBooking = bookingRepository.save(booking);

        ra.addFlashAttribute("bookingId", savedBooking.getId());
        return "redirect:/booking/success";
    }

    @GetMapping("/booking/success")
    public String success() {
        return "success";
    }
}