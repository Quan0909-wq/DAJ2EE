package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Booking;
import com.example.webcinemabooking.model.Showtime;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.BookingRepository;
import com.example.webcinemabooking.repository.ProductRepository;
import com.example.webcinemabooking.repository.ShowtimeRepository;
import com.example.webcinemabooking.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BookingController {

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private EmailService emailService;

    // 1. Hiển thị sơ đồ ghế
    @GetMapping("/booking/{id}")
    public String showSeatMap(@PathVariable("id") Long id, Model model) {
        Showtime showtime = showtimeRepository.findById(id).orElse(null);
        if (showtime == null) return "redirect:/";

        List<Booking> bookings = bookingRepository.findByShowtimeId(id);
        String occupiedSeats = bookings.stream()
                .map(Booking::getSeatNumbers)
                .collect(Collectors.joining(","));

        model.addAttribute("showtime", showtime);
        model.addAttribute("occupiedSeats", occupiedSeats);
        model.addAttribute("rows", showtime.getRoom().getTotalRows());
        model.addAttribute("cols", showtime.getRoom().getTotalCols());
        model.addAttribute("products", productRepository.findAll());

        return "booking";
    }

    // 2. Chuyển sang trang nhập thông tin (Checkout)
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
        model.addAttribute("totalAmount", totalAmount);

        return "checkout";
    }

    // 3. XÁC NHẬN VÀ LƯU VÉ TRỰC TIẾP (Bỏ qua thanh toán online)
    @PostMapping("/booking/confirm")
    public String confirmBooking(@RequestParam Long showtimeId,
                                 @RequestParam String seats,
                                 @RequestParam double totalAmount,
                                 @RequestParam String customerName,
                                 @RequestParam String customerPhone,
                                 @RequestParam String customerEmail,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";

        // Tạo đối tượng Booking và lưu trực tiếp vào database
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumbers(seats);
        booking.setTotalAmount(totalAmount);
        booking.setCustomerName(customerName);
        booking.setCustomerPhone(customerPhone);
        booking.setCustomerEmail(customerEmail);
        booking.setBookingTime(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Gửi email vé điện tử cho khách
        try {
            emailService.sendTicketEmail(savedBooking);
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail: " + e.getMessage());
        }

        ra.addFlashAttribute("bookingId", savedBooking.getId());
        return "redirect:/booking/success";
    }

    // 4. Trang thông báo thành công
    @GetMapping("/booking/success")
    public String success() {
        return "success";
    }

    // 5. Lịch sử vé của tôi
    @GetMapping("/my-tickets")
    public String myBookings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<Booking> myBookings = bookingRepository.findByCustomerEmailOrderByBookingTimeDesc(loggedInUser.getEmail());
        model.addAttribute("bookings", myBookings);

        return "my-tickets";
    }
    @GetMapping("/booking/early/{id}")
    public String showEarlyBookingPage(@PathVariable Long id, Model model) {
        // Chuyển ID phim ra ngoài giao diện để hiển thị
        model.addAttribute("movieId", id);

        // Gọi đến file early-booking.html
        return "early-booking";
    }
}