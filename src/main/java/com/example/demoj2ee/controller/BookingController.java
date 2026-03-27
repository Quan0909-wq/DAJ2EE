package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.*;
import com.example.demoj2ee.repository.*;
import com.example.demoj2ee.service.GroupBookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
public class BookingController {

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private GroupBookingService groupBookingService;
    @Autowired private PromotionRepository promotionRepository;

    // 1. Hiển thị sơ đồ ghế
    @GetMapping("/booking/{id}")
    public String showSeatMap(@PathVariable("id") Long id, Model model) {
        Showtime showtime = showtimeRepository.findById(id).orElse(null);
        if (showtime == null) return "redirect:/";

        Set<String> occupiedSeats = groupBookingService.getOccupiedSeats(showtime.getId(), null);

        model.addAttribute("showtime", showtime);
        model.addAttribute("rows", 10);
        model.addAttribute("cols", 10);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("occupiedSeats", String.join(",", occupiedSeats));

        return "booking";
    }

    // 2. Chuyển sang trang nhập thông tin + load promotions từ DB
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
        model.addAttribute("originalAmount", totalAmount);
        model.addAttribute("totalAmount", totalAmount);

        // Load promotions đang hoạt động và chưa hết hạn
        var promotions = promotionRepository
                .findByActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime.now());
        model.addAttribute("activePromotions", promotions);

        return "checkout";
    }

    // API: kiểm tra & tính giảm giá khi nhập mã khuyến mãi
    @PostMapping("/booking/apply-promo")
    @ResponseBody
    public Map<String, Object> applyPromo(@RequestParam String code,
                                           @RequestParam double originalAmount,
                                           @RequestParam(required = false) Long showtimeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        var promoOpt = promotionRepository.findValidByCode(code.toUpperCase().trim(), LocalDateTime.now());
        if (promoOpt.isEmpty()) {
            result.put("message", "Mã khuyến mãi không hợp lệ hoặc đã hết hạn!");
            return result;
        }

        Promotion promo = promoOpt.get();

        // Nếu khuyến mãi gắn với suất chiếu cụ thể, kiểm tra
        if (promo.getShowtime() != null && showtimeId != null
                && !promo.getShowtime().getId().equals(showtimeId)) {
            result.put("message", "Mã này chỉ áp dụng cho suất chiếu cụ thể!");
            return result;
        }

        double discount = promo.calculateDiscount(originalAmount);
        double newTotal = Math.max(0, originalAmount - discount);

        result.put("success", true);
        result.put("discount", discount);
        result.put("newTotal", newTotal);
        result.put("discountText", promo.getDiscountAmount() > 0
                ? "Giảm " + String.format("%.0f", promo.getDiscountAmount()) + "đ"
                : "Giảm " + promo.getDiscountPercent() + "%");
        return result;
    }

    // 3. XÁC NHẬN VÀ LƯU VÉ
    @PostMapping("/booking/confirm")
    public String confirmBooking(@RequestParam Long showtimeId,
                                 @RequestParam String seats,
                                 @RequestParam(defaultValue = "0") double foodAmount,
                                 @RequestParam String customerName,
                                 @RequestParam String customerEmail,
                                 @RequestParam(required = false) String customerPhone,
                                 @RequestParam double finalAmount,
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

        booking.setCustomerName(customerName);
        booking.setCustomerEmail(customerEmail);
        if (customerPhone != null && !customerPhone.isBlank()) {
            booking.setCustomerPhone(customerPhone);
        }

        Booking savedBooking = bookingRepository.save(booking);

        ra.addFlashAttribute("bookingId", savedBooking.getId());
        return "redirect:/booking/success";
    }

    @GetMapping("/booking/confirm")
    public String confirmBookingGet() {
        return "redirect:/";
    }

    @GetMapping("/booking/success")
    public String success() {
        return "success";
    }
}