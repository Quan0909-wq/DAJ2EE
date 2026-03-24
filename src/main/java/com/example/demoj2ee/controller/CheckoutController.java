package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Booking;
import com.example.demoj2ee.model.Food;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.BookingRepository;
import com.example.demoj2ee.repository.FoodRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private BookingRepository bookingRepository; // Cần cái này để lưu vé xuống DB

    // 1. Mở trang Checkout (Đã làm ở bước trước)
    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        String seatName = (String) session.getAttribute("selectedSeats");
        Integer ticketPrice = (Integer) session.getAttribute("ticketPrice");
        String movieName = (String) session.getAttribute("movieName");

        if (seatName == null || ticketPrice == null) {
            return "redirect:/book-seat";
        }

        List<Food> foods = foodRepository.findAll();
        model.addAttribute("foods", foods);
        model.addAttribute("seatName", seatName);
        model.addAttribute("ticketPrice", ticketPrice);
        model.addAttribute("movieName", movieName);

        return "checkout";
    }

    // 2. CHỐT ĐƠN: Xử lý khi khách bấm THANH TOÁN NGAY
    @PostMapping("/process-payment")
    public String processPayment(@RequestParam("totalAmount") double totalAmount,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("loggedInUser");
        String seats = (String) session.getAttribute("selectedSeats");
        String movieName = (String) session.getAttribute("movieName");

        // Tạo hóa đơn mới
        Booking newBooking = new Booking();
        newBooking.setCustomerName(user.getFullName());
        newBooking.setSeatNumbers(seats);
        newBooking.setTotalAmount(totalAmount);
        newBooking.setBookingTime(LocalDateTime.now());
        // Tùy theo bảng Booking của sếp thiết kế mà sếp set thuộc tính cho đúng nhé

        // Lưu vào Database
        bookingRepository.save(newBooking);

        // Xóa trí nhớ tạm đi để đặt vé khác không bị dính nợ cũ
        session.removeAttribute("selectedSeats");
        session.removeAttribute("ticketPrice");
        session.removeAttribute("movieName");

        // Báo thành công và đá về trang chủ
        redirectAttributes.addFlashAttribute("message", "Thanh toán thành công! Chúc sếp xem phim vui vẻ nha.");
        return "redirect:/";
    }
}