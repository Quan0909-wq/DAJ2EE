package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Booking;
import com.example.webcinemabooking.model.TicketPass;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.BookingRepository;
import com.example.webcinemabooking.repository.TicketPassRepository;
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

    // 1. HIỂN THỊ CHỢ VÀ CÁC GIAO DỊCH ĐANG CHỜ / TRANH CHẤP
    @GetMapping("/ticket-market")
    public String showMarket(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/login";
        model.addAttribute("currentUser", currentUser);

        // 1. Lấy vé đang bày bán công khai (AVAILABLE)
        List<TicketPass> availableTickets = ticketPassRepository.findByStatusOrderByCreatedAtDesc("AVAILABLE");
        model.addAttribute("tickets", availableTickets);

        // 2. Lấy vé liên quan đến sếp (Để xử lý Giam vé/Tranh chấp)
        List<TicketPass> myLockedTickets = new ArrayList<>();
        // 3. [MỚI] Lấy toàn bộ lịch sử sếp đăng bán (Để sếp biết mình đã pass vé nào)
        List<TicketPass> myHistory = new ArrayList<>();

        List<TicketPass> all = ticketPassRepository.findAll();
        for (TicketPass tp : all) {
            // Lọc vé đang kẹt (Giam hoặc kiện cáo)
            if (("LOCKED".equals(tp.getStatus()) || "DISPUTED".equals(tp.getStatus())) &&
                    (tp.getSeller().getId().equals(currentUser.getId()) ||
                            (tp.getBuyer() != null && tp.getBuyer().getId().equals(currentUser.getId())))) {
                myLockedTickets.add(tp);
            }

            // Lọc lịch sử: Cứ sếp là người bán thì cho vào danh sách "Của tôi"
            if (tp.getSeller().getId().equals(currentUser.getId())) {
                myHistory.add(tp);
            }
        }

        model.addAttribute("lockedTickets", myLockedTickets);
        model.addAttribute("myHistory", myHistory); // Gửi cái này qua HTML

        return "ticket-market";
    }

    // 2. KHÁCH ĐĂNG BÁN VÉ
    @PostMapping("/ticket-market/post")
    public String postTicket(@RequestParam("bookingId") Long bookingId,
                             @RequestParam("passPrice") double passPrice,
                             @RequestParam("reason") String reason,
                             @RequestParam("contactInfo") String contactInfo,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/login";

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getCustomerEmail() == null || !booking.getCustomerEmail().equalsIgnoreCase(currentUser.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã hóa đơn không tồn tại hoặc Email không khớp!");
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
        redirectAttributes.addFlashAttribute("successMessage", "Đăng pass vé thành công! Chờ người hốt thôi!");
        return "redirect:/ticket-market";
    }

    // 3. KHÁCH KHÁC BẤM MUA VÉ -> GIAM VÉ LẠI CHỜ TIỀN
    @PostMapping("/ticket-market/buy")
    public String buyTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/login";

        TicketPass ticketPass = ticketPassRepository.findById(passId).orElse(null);
        if (ticketPass != null && "AVAILABLE".equals(ticketPass.getStatus())) {
            if (ticketPass.getSeller().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể tự mua lại vé của chính mình!");
                return "redirect:/ticket-market";
            }
            ticketPass.setStatus("LOCKED");
            ticketPass.setBuyer(currentUser);
            ticketPassRepository.save(ticketPass);
            redirectAttributes.addFlashAttribute("successMessage", "Đã giam vé! Vui lòng liên hệ chuyển tiền cho người bán để họ Xác Nhận.");
        }
        return "redirect:/ticket-market";
    }

    // 4. NGƯỜI BÁN XÁC NHẬN CÓ TIỀN -> SANG TÊN VÉ
    @PostMapping("/ticket-market/confirm")
    public String confirmTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        TicketPass ticketPass = ticketPassRepository.findById(passId).orElse(null);

        // Dù đang bị khóa hay tranh chấp, nếu người bán nhả vé thì vẫn cho qua
        if (ticketPass != null && ("LOCKED".equals(ticketPass.getStatus()) || "DISPUTED".equals(ticketPass.getStatus()))
                && ticketPass.getSeller().getId().equals(currentUser.getId())) {

            ticketPass.setStatus("SOLD");
            ticketPassRepository.save(ticketPass);

            Booking booking = ticketPass.getBooking();
            booking.setCustomerName(ticketPass.getBuyer().getUsername() + " (Mua Pass)");
            booking.setCustomerEmail(ticketPass.getBuyer().getEmail());
            bookingRepository.save(booking);

            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thành công! Vé đã được sang tên cho người mua.");
        }
        return "redirect:/ticket-market";
    }

    // 5. HỦY KÈO -> NHẢ VÉ RA LẠI CHỢ
    @PostMapping("/ticket-market/cancel")
    public String cancelTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        TicketPass ticketPass = ticketPassRepository.findById(passId).orElse(null);

        if (ticketPass != null && "LOCKED".equals(ticketPass.getStatus())) {
            if (ticketPass.getSeller().getId().equals(currentUser.getId()) || ticketPass.getBuyer().getId().equals(currentUser.getId())) {
                ticketPass.setStatus("AVAILABLE");
                ticketPass.setBuyer(null);
                ticketPassRepository.save(ticketPass);
                redirectAttributes.addFlashAttribute("errorMessage", "Giao dịch đã bị hủy, vé được nhả lại ra chợ!");
            }
        }
        return "redirect:/ticket-market";
    }

    // 6. [TÍNH NĂNG MỚI] NGƯỜI MUA BẤM BÁO CÁO LỪA ĐẢO
    @PostMapping("/ticket-market/report")
    public String reportTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        TicketPass ticketPass = ticketPassRepository.findById(passId).orElse(null);

        if (ticketPass != null && "LOCKED".equals(ticketPass.getStatus()) && ticketPass.getBuyer().getId().equals(currentUser.getId())) {

            // Chuyển sang trạng thái TRANH CHẤP
            ticketPass.setStatus("DISPUTED");
            ticketPassRepository.save(ticketPass);

            redirectAttributes.addFlashAttribute("errorMessage", "🚨 BÁO CÁO THÀNH CÔNG! Giao dịch đã bị đóng băng. Admin Boss Quan sẽ vào cuộc điều tra!");
        }
        return "redirect:/ticket-market";
    }
    // HÀM GỠ BÀI ĐĂNG (Xóa vé khỏi chợ)
    @PostMapping("/ticket-market/remove")
    public String removeTicket(@RequestParam("passId") Long passId, HttpSession session, RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        TicketPass tp = ticketPassRepository.findById(passId).orElse(null);

        if (tp != null && "AVAILABLE".equals(tp.getStatus()) && tp.getSeller().getId().equals(currentUser.getId())) {
            ticketPassRepository.delete(tp);
            ra.addFlashAttribute("successMessage", "Đã gỡ bài đăng và thu hồi vé thành công!");
        } else {
            ra.addFlashAttribute("errorMessage", "Không thể gỡ bài đăng khi vé đang có người giao dịch!");
        }
        return "redirect:/ticket-market";
    }
}