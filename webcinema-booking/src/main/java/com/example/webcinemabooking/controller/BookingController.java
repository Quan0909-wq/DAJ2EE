package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Booking;
import com.example.webcinemabooking.model.Showtime;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.BookingRepository;
import com.example.webcinemabooking.repository.ProductRepository;
import com.example.webcinemabooking.repository.ShowtimeRepository;
import com.example.webcinemabooking.repository.UserRepository; // Đã thêm
import com.example.webcinemabooking.service.EmailService;
import com.example.webcinemabooking.service.PeleBookingService; // Đã thêm
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BookingController {

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;

    // --- Sửa lỗi thiếu tên biến ở đây ---
    @Autowired private PeleBookingService peleBookingService;

    // 1. Hiển thị sơ đồ ghế (GIỮ NGUYÊN)
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

    // 2. Chuyển sang trang nhập thông tin (GIỮ NGUYÊN)
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

    // 3. XÁC NHẬN VÀ LƯU VÉ (Bản gộp: Đã thêm logic thăng hạng & tính tiền bắp nước)
    @PostMapping("/booking/confirm")
    public String confirmBooking(@RequestParam Long showtimeId,
                                 @RequestParam String seats,
                                 @RequestParam(defaultValue = "0") double foodAmount, // Mặc định là 0 nếu không gửi
                                 @RequestParam String customerName,
                                 @RequestParam String customerPhone,
                                 @RequestParam String customerEmail,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // --- Logic tính toán tiền (Chống hack & Khuyến mãi) ---
        String[] seatArray = seats.split(",");
        int seatCount = seatArray.length;
        double ticketPrice = showtime.getPrice();

        // Tính tiền chuẩn từ Service
        double finalAmount = peleBookingService.calculateTotalBill(loggedInUser, seatCount, ticketPrice, foodAmount);

        // Tạo đối tượng Booking
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumbers(seats);
        booking.setTotalAmount(finalAmount);
        booking.setCustomerName(customerName);
        booking.setCustomerPhone(customerPhone);
        booking.setCustomerEmail(customerEmail);
        booking.setBookingTime(LocalDateTime.now());
        if (loggedInUser != null) booking.setUser(loggedInUser);

        Booking savedBooking = bookingRepository.save(booking);

        // --- Logic thăng hạng Đế Chế PELE ---
        if (loggedInUser != null) {
            int newTotal = loggedInUser.getTotalTicketsBought() + seatCount;
            loggedInUser.setTotalTicketsBought(newTotal);

            // Cập nhật hạng mới (Ghoul -> Hunter -> Lord)
            String newRank = peleBookingService.updatePeleRank(newTotal);
            loggedInUser.setPeleRank(newRank);

            userRepository.save(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);
        }

        // Gửi email vé điện tử
        try {
            emailService.sendTicketEmail(savedBooking);
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail: " + e.getMessage());
        }

        ra.addFlashAttribute("bookingId", savedBooking.getId());
        return "redirect:/booking/success";
    }

    // 4. Trang thông báo thành công (GIỮ NGUYÊN)
    @GetMapping("/booking/success")
    public String success() {
        return "success";
    }

    // 5. Lịch sử vé của tôi (GIỮ NGUYÊN)
    @GetMapping("/my-tickets")
    public String myBookings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<Booking> myBookings = bookingRepository.findByCustomerEmailIgnoreCaseOrderByBookingTimeDesc(loggedInUser.getEmail());
        model.addAttribute("bookings", myBookings);

        return "my-tickets";
    }

    // 6. Đặt vé sớm (GIỮ NGUYÊN)
    @GetMapping("/booking/early/{id}")
    public String showEarlyBookingPage(@PathVariable Long id, Model model) {
        model.addAttribute("movieId", id);
        return "early-booking";
    }
}