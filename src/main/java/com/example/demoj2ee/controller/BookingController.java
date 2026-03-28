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
import java.util.*;

@Controller
public class BookingController {

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private GroupBookingService groupBookingService;
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private TicketDisputeRepository ticketDisputeRepository;
    @Autowired private MovieRepository movieRepository;

    private Booking getOwnedBookingOrNull(Long bookingId, User loggedInUser) {
        if (bookingId == null || loggedInUser == null) return null;
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getUser() == null) return null;
        if (!booking.getUser().getId().equals(loggedInUser.getId())) return null;
        return booking;
    }

    @GetMapping("/booking/{id}")
    public String showSeatMap(@PathVariable("id") Long id,
                              @RequestParam(required = false) Long bookingId,
                              @RequestParam(required = false, defaultValue = "false") boolean changeMode,
                              @RequestParam(required = false) String actionType,
                              HttpSession session,
                              Model model,
                              RedirectAttributes ra) {
        Showtime showtime = showtimeRepository.findById(id).orElse(null);
        if (showtime == null) return "redirect:/";

        Booking booking = null;
        if (changeMode) {
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser == null) return "redirect:/login";

            booking = getOwnedBookingOrNull(bookingId, loggedInUser);
            if (booking == null) {
                ra.addFlashAttribute("error", "Không tìm thấy vé cần cập nhật!");
                return "redirect:/my-tickets";
            }

            boolean allowed = switch (actionType == null ? "" : actionType) {
                case "CHANGE_SEAT" -> booking.isAllowSeatChange();
                case "CHANGE_SHOWTIME" -> booking.isAllowShowtimeChange();
                case "CHANGE_MOVIE" -> booking.isAllowMovieChange();
                default -> false;
            };

            if (!allowed) {
                ra.addFlashAttribute("error", "Vé này chưa được admin cho phép thao tác!");
                return "redirect:/my-tickets";
            }
        }

        Set<String> occupiedSeats = new LinkedHashSet<>(groupBookingService.getOccupiedSeats(showtime.getId(), null));

        if (changeMode && booking != null && booking.getShowtime() != null
                && booking.getShowtime().getId().equals(showtime.getId())
                && booking.getSeatNumbers() != null) {
            for (String s : booking.getSeatNumbers().split(",")) {
                occupiedSeats.remove(s.trim());
            }
        }

        model.addAttribute("showtime", showtime);
        model.addAttribute("rows", 10);
        model.addAttribute("cols", 10);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("occupiedSeats", String.join(",", occupiedSeats));
        model.addAttribute("changeMode", changeMode);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("oldSeats", booking != null ? booking.getSeatNumbers() : "");
        model.addAttribute("actionType", actionType != null ? actionType : "");

        return "booking";
    }

    @PostMapping("/booking/checkout")
    public String showCheckoutPage(@RequestParam Long showtimeId,
                                   @RequestParam String seats,
                                   @RequestParam double totalAmount,
                                   @RequestParam(required = false) Long bookingId,
                                   @RequestParam(required = false, defaultValue = "false") boolean changeMode,
                                   @RequestParam(required = false) String actionType,
                                   HttpSession session,
                                   Model model) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        Booking oldBooking = null;
        if (changeMode && bookingId != null && loggedInUser != null) {
            oldBooking = getOwnedBookingOrNull(bookingId, loggedInUser);
        }

        model.addAttribute("user", loggedInUser);
        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("originalAmount", totalAmount);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("changeMode", changeMode);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("actionType", actionType != null ? actionType : "");
        model.addAttribute("oldBooking", oldBooking);

        var promotions = promotionRepository
                .findByActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime.now());
        model.addAttribute("activePromotions", promotions);

        return "checkout";
    }

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

    @PostMapping("/booking/confirm")
    public String confirmBooking(@RequestParam Long showtimeId,
                                 @RequestParam String seats,
                                 @RequestParam(defaultValue = "0") double foodAmount,
                                 @RequestParam String customerName,
                                 @RequestParam String customerEmail,
                                 @RequestParam(required = false) String customerPhone,
                                 @RequestParam double finalAmount,
                                 @RequestParam(required = false) Long bookingId,
                                 @RequestParam(required = false, defaultValue = "false") boolean changeMode,
                                 @RequestParam(required = false) String actionType,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (changeMode && bookingId != null) {
            Booking booking = getOwnedBookingOrNull(bookingId, loggedInUser);
            if (booking == null) {
                ra.addFlashAttribute("error", "Không tìm thấy vé cần cập nhật!");
                return "redirect:/my-tickets";
            }

            booking.setShowtime(showtime);
            booking.setSeatNumbers(seats);
            booking.setTotalAmount(finalAmount);
            booking.setBookingTime(LocalDateTime.now());
            booking.setCustomerName(customerName);
            booking.setCustomerEmail(customerEmail);
            if (customerPhone != null && !customerPhone.isBlank()) {
                booking.setCustomerPhone(customerPhone);
            }

            booking.setAllowSeatChange(false);
            booking.setAllowShowtimeChange(false);
            booking.setAllowMovieChange(false);
            booking.setAllowCancelBooking(false);

            String actionLabel = switch (actionType == null ? "" : actionType) {
                case "CHANGE_SEAT" -> "Đã đổi ghế thành công sang: " + seats;
                case "CHANGE_SHOWTIME" -> "Đã đổi suất chiếu thành công.";
                case "CHANGE_MOVIE" -> "Đã đổi phim thành công.";
                default -> "Đã cập nhật vé thành công.";
            };
            booking.setDisputeNote(actionLabel);
            bookingRepository.save(booking);

            TicketDispute dispute = ticketDisputeRepository
                    .findTopByBookingIdOrderByCreatedAtDesc(booking.getId())
                    .orElse(null);
            if (dispute != null) {
                dispute.setCustomerActionDone(true);
                dispute.setCustomerActionRequired(false);
                ticketDisputeRepository.save(dispute);
            }

            ra.addFlashAttribute("success", "Cập nhật vé thành công! Vé cũ đã được thay đổi trực tiếp.");
            return "redirect:/my-tickets";
        }

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

    @GetMapping("/booking/change-seat/{id}")
    public String changeSeat(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes ra) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Booking booking = getOwnedBookingOrNull(id, loggedInUser);
        if (booking == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé!");
            return "redirect:/my-tickets";
        }

        if (!booking.isAllowSeatChange()) {
            ra.addFlashAttribute("error", "Vé này chưa được admin cho phép đổi ghế!");
            return "redirect:/my-tickets";
        }

        return "redirect:/booking/" + booking.getShowtime().getId()
                + "?bookingId=" + booking.getId()
                + "&changeMode=true&actionType=CHANGE_SEAT";
    }

    @GetMapping("/booking/change-showtime/{id}")
    public String changeShowtime(@PathVariable Long id,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes ra) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Booking booking = getOwnedBookingOrNull(id, loggedInUser);
        if (booking == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé!");
            return "redirect:/my-tickets";
        }

        if (!booking.isAllowShowtimeChange()) {
            ra.addFlashAttribute("error", "Vé này chưa được admin cho phép đổi suất!");
            return "redirect:/my-tickets";
        }

        List<Showtime> showtimes = showtimeRepository
                .findByMovieIdAndStartTimeAfterOrderByStartTimeAsc(
                        booking.getShowtime().getMovie().getId(), LocalDateTime.now());

        showtimes = showtimes.stream()
                .filter(s -> !s.getId().equals(booking.getShowtime().getId()))
                .toList();

        model.addAttribute("booking", booking);
        model.addAttribute("showtimes", showtimes);
        return "change-showtime";
    }

    @GetMapping("/booking/change-showtime/select/{showtimeId}")
    public String selectNewShowtime(@PathVariable Long showtimeId,
                                    @RequestParam Long bookingId,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Booking booking = getOwnedBookingOrNull(bookingId, loggedInUser);
        if (booking == null || !booking.isAllowShowtimeChange()) {
            ra.addFlashAttribute("error", "Bạn không thể đổi suất cho vé này!");
            return "redirect:/my-tickets";
        }

        return "redirect:/booking/" + showtimeId
                + "?bookingId=" + booking.getId()
                + "&changeMode=true&actionType=CHANGE_SHOWTIME";
    }

    @GetMapping("/booking/change-movie/{id}")
    public String changeMovie(@PathVariable Long id,
                              HttpSession session,
                              Model model,
                              RedirectAttributes ra) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Booking booking = getOwnedBookingOrNull(id, loggedInUser);
        if (booking == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé!");
            return "redirect:/my-tickets";
        }

        if (!booking.isAllowMovieChange()) {
            ra.addFlashAttribute("error", "Vé này chưa được admin cho phép đổi phim!");
            return "redirect:/my-tickets";
        }

        List<Movie> movies = movieRepository.findAll();

        Map<Long, List<Showtime>> showtimeMap = new HashMap<>();
        for (Movie movie : movies) {
            List<Showtime> movieShowtimes = showtimeRepository
                    .findByMovieIdAndStartTimeAfterOrderByStartTimeAsc(movie.getId(), LocalDateTime.now());
            if (!movieShowtimes.isEmpty()) {
                showtimeMap.put(movie.getId(), movieShowtimes);
            }
        }

        model.addAttribute("booking", booking);
        model.addAttribute("movies", movies);
        model.addAttribute("showtimeMap", showtimeMap);
        return "change-movie";
    }

    @GetMapping("/booking/change-movie/select/{showtimeId}")
    public String selectNewMovieShowtime(@PathVariable Long showtimeId,
                                         @RequestParam Long bookingId,
                                         HttpSession session,
                                         RedirectAttributes ra) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Booking booking = getOwnedBookingOrNull(bookingId, loggedInUser);
        if (booking == null || !booking.isAllowMovieChange()) {
            ra.addFlashAttribute("error", "Bạn không thể đổi phim cho vé này!");
            return "redirect:/my-tickets";
        }

        return "redirect:/booking/" + showtimeId
                + "?bookingId=" + booking.getId()
                + "&changeMode=true&actionType=CHANGE_MOVIE";
    }

    @GetMapping("/booking/cancel/{id}")
    public String cancelBooking(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes ra) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Booking booking = getOwnedBookingOrNull(id, loggedInUser);
        if (booking == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé!");
            return "redirect:/my-tickets";
        }

        if (!booking.isAllowCancelBooking()) {
            ra.addFlashAttribute("error", "Vé này chưa được admin cho phép hủy!");
            return "redirect:/my-tickets";
        }

        booking.setStatus("CANCELLED");
        booking.setAllowCancelBooking(false);
        booking.setAllowSeatChange(false);
        booking.setAllowShowtimeChange(false);
        booking.setAllowMovieChange(false);
        bookingRepository.save(booking);

        TicketDispute dispute = ticketDisputeRepository
                .findTopByBookingIdOrderByCreatedAtDesc(booking.getId())
                .orElse(null);
        if (dispute != null) {
            dispute.setCustomerActionDone(true);
            dispute.setCustomerActionRequired(false);
            ticketDisputeRepository.save(dispute);
        }

        ra.addFlashAttribute("success", "Hủy vé thành công!");
        return "redirect:/my-tickets";
    }
}
