package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Booking;
import com.example.demoj2ee.model.TicketPass;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.BookingRepository;
import com.example.demoj2ee.repository.TicketPassRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TicketMarketController {

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/ticket-market")
    public String showMarket(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/login";
        model.addAttribute("currentUser", currentUser);

        List<TicketPass> availableTickets = ticketPassRepository.findByStatusOrderByCreatedAtDesc("AVAILABLE");
        model.addAttribute("tickets", availableTickets);

        List<TicketPass> myLockedTickets = new ArrayList<>();
        List<TicketPass> myHistory = new ArrayList<>();

        List<TicketPass> all = ticketPassRepository.findAll();
        for (TicketPass tp : all) {
            if (("LOCKED".equals(tp.getStatus()) || "DISPUTED".equals(tp.getStatus())) &&
                    (tp.getSeller().getId().equals(currentUser.getId()) ||
                            (tp.getBuyer() != null && tp.getBuyer().getId().equals(currentUser.getId())))) {
                myLockedTickets.add(tp);
            }
            if (tp.getSeller().getId().equals(currentUser.getId())) {
                myHistory.add(tp);
            }
        }

        model.addAttribute("lockedTickets", myLockedTickets);
        model.addAttribute("myHistory", myHistory);

        return "ticket-market";
    }

    @PostMapping("/ticket-market/post")
    public String postTicket(@RequestParam("bookingId") Long bookingId,
                             @RequestParam("passPrice") double passPrice,
                             @RequestParam("reason") String reason,
                             @RequestParam("contactInfo") String contactInfo,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/login";

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getCustomerEmail() == null ||
                !booking.getCustomerEmail().equalsIgnoreCase(currentUser.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ma hoa don khong ton tai hoac Email khong khop!");
            return "redirect:/ticket-market";
        }

        TicketPass ticketPass = new TicketPass();
        ticketPass.setSeller(currentUser);
        ticketPass.setBooking(booking);
        ticketPass.setPassPrice(passPrice);
        ticketPass.setReason(reason);
        ticketPass.setContactInfo(contactInfo);
        ticketPass.setStatus("AVAILABLE");

        ticketPassRepository.save(ticketPass);
        redirectAttributes.addFlashAttribute("successMessage", "Dang pass ve thanh cong!");
        return "redirect:/ticket-market";
    }

    @PostMapping("/ticket-market/buy")
    public String buyTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/login";

        TicketPass ticketPass = ticketPassRepository.findById(passId).orElse(null);
        if (ticketPass != null && "AVAILABLE".equals(ticketPass.getStatus())) {
            if (ticketPass.getSeller().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Khong the tu mua lai ve cua chinh minh!");
                return "redirect:/ticket-market";
            }
            ticketPass.setStatus("LOCKED");
            ticketPass.setBuyer(currentUser);
            ticketPassRepository.save(ticketPass);
            redirectAttributes.addFlashAttribute("successMessage", "Da giam ve! Vui long lien he chuyen tien cho nguoi ban.");
        }
        return "redirect:/ticket-market";
    }

    @PostMapping("/ticket-market/confirm")
    public String confirmTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        TicketPass ticketPass = ticketPassRepository.findById(passId).orElse(null);

        if (ticketPass != null && ("LOCKED".equals(ticketPass.getStatus()) || "DISPUTED".equals(ticketPass.getStatus()))
                && ticketPass.getSeller().getId().equals(currentUser.getId())) {

            ticketPass.setStatus("SOLD");
            ticketPassRepository.save(ticketPass);

            Booking booking = ticketPass.getBooking();
            booking.setCustomerName(ticketPass.getBuyer().getUsername() + " (Mua Pass)");
            booking.setCustomerEmail(ticketPass.getBuyer().getEmail());
            bookingRepository.save(booking);

            redirectAttributes.addFlashAttribute("successMessage", "Xac nhan thanh cong! Ve da duoc sang ten.");
        }
        return "redirect:/ticket-market";
    }

    @PostMapping("/ticket-market/cancel")
    public String cancelTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        TicketPass ticketPass = ticketPassRepository.findById(passId).orElse(null);

        if (ticketPass != null && "LOCKED".equals(ticketPass.getStatus())) {
            if (ticketPass.getSeller().getId().equals(currentUser.getId()) ||
                    ticketPass.getBuyer().getId().equals(currentUser.getId())) {
                ticketPass.setStatus("AVAILABLE");
                ticketPass.setBuyer(null);
                ticketPassRepository.save(ticketPass);
                redirectAttributes.addFlashAttribute("errorMessage", "Giao dich da bi huy, ve duoc tra lai ra cho!");
            }
        }
        return "redirect:/ticket-market";
    }

    @PostMapping("/ticket-market/report")
    public String reportTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        TicketPass ticketPass = ticketPassRepository.findById(passId).orElse(null);

        if (ticketPass != null && "LOCKED".equals(ticketPass.getStatus()) &&
                ticketPass.getBuyer().getId().equals(currentUser.getId())) {

            ticketPass.setStatus("DISPUTED");
            ticketPassRepository.save(ticketPass);

            redirectAttributes.addFlashAttribute("errorMessage", "Bao cao thanh cong! Giao dich da bi dong bang. Admin se vao cuoc dieu tra!");
        }
        return "redirect:/ticket-market";
    }

    @PostMapping("/ticket-market/remove")
    public String removeTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        TicketPass tp = ticketPassRepository.findById(passId).orElse(null);

        if (tp != null && "AVAILABLE".equals(tp.getStatus()) && tp.getSeller().getId().equals(currentUser.getId())) {
            ticketPassRepository.delete(tp);
            ra.addFlashAttribute("successMessage", "Da goi bai dang va thu hoi ve thanh cong!");
        } else {
            ra.addFlashAttribute("errorMessage", "Khong the goi bai dang khi ve dang co nguoi giao dich!");
        }
        return "redirect:/ticket-market";
    }
}
