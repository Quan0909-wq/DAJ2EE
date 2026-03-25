package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Booking;
import com.example.demoj2ee.model.Showtime;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.BookingRepository;
import com.example.demoj2ee.repository.ProductRepository;
import com.example.demoj2ee.repository.ShowtimeRepository;
import com.example.demoj2ee.repository.UserRepository;
import com.example.demoj2ee.service.EmailService;
import com.example.demoj2ee.service.PeleBookingService;
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
    @Autowired private PeleBookingService peleBookingService;

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

    @PostMapping("/booking/confirm")
    public String confirmBooking(@RequestParam Long showtimeId,
                                 @RequestParam String seats,
                                 @RequestParam(defaultValue = "0") double foodAmount,
                                 @RequestParam String customerName,
                                 @RequestParam String customerPhone,
                                 @RequestParam String customerEmail,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        String[] seatArray = seats.split(",");
        int seatCount = seatArray.length;
        double ticketPrice = showtime.getPrice();

        double finalAmount = peleBookingService.calculateTotalBill(loggedInUser, seatCount, ticketPrice, foodAmount);

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

        if (loggedInUser != null) {
            int newTotal = loggedInUser.getTotalTicketsBought() + seatCount;
            loggedInUser.setTotalTicketsBought(newTotal);
            String newRank = peleBookingService.updatePeleRank(newTotal);
            loggedInUser.setPeleRank(newRank);
            userRepository.save(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);
        }

        try {
            emailService.sendTicketEmail(savedBooking);
        } catch (Exception e) {
            System.out.println("Loi gui mail: " + e.getMessage());
        }

        ra.addFlashAttribute("bookingId", savedBooking.getId());
        return "redirect:/booking/success";
    }

    @GetMapping("/booking/success")
    public String success() {
        return "success";
    }

    @GetMapping("/my-tickets")
    public String myBookings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<Booking> myBookings = bookingRepository.findByCustomerEmailIgnoreCaseOrderByBookingTimeDesc(loggedInUser.getEmail());
        model.addAttribute("bookings", myBookings);

        return "my-tickets";
    }

    @GetMapping("/booking/early/{id}")
    public String showEarlyBookingPage(@PathVariable Long id, Model model) {
        model.addAttribute("movieId", id);
        return "early-booking";
    }
}
