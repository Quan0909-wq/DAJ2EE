package com.example.demoj2ee.service;

import com.example.demoj2ee.model.*;
import com.example.demoj2ee.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TicketPassService {

    @Autowired private TicketPassRepository ticketPassRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ShowtimeRepository showtimeRepository;

    // ============================================================
    // TAO TICKET PASS (DANG BAN VE)
    // ============================================================

    @Transactional
    public TicketPass createPass(Long bookingId, Long sellerId, double passPrice,
                                  String reason, String contactInfo) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) throw new RuntimeException("Không tìm thấy vé");

        User seller = userRepository.findById(sellerId).orElse(null);
        if (seller == null) throw new RuntimeException("Không tìm thấy người bán");

        // Kiem tra quyen so huu
        if (booking.getUser() != null && !booking.getUser().getId().equals(sellerId)) {
            throw new RuntimeException("Bạn không sở hữu vé này");
        }

        // Kiem tra suat chieu chua dien ra
        if (booking.getShowtime() != null &&
            booking.getShowtime().getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Suất chiếu đã diễn ra, không thể pass vé");
        }

        // Kiem tra da co pass chua
        List<TicketPass> existing = ticketPassRepository.findByBookingId(bookingId);
        for (TicketPass p : existing) {
            if ("AVAILABLE".equals(p.getStatus()) || "LOCKED".equals(p.getStatus())) {
                throw new RuntimeException("Vé này đã được đăng bán trước đó");
            }
        }

        TicketPass pass = new TicketPass();
        pass.setBooking(booking);
        pass.setSeller(seller);
        pass.setPassPrice(passPrice);
        pass.setReason(reason);
        pass.setContactInfo(contactInfo);
        pass.setStatus("AVAILABLE");
        pass.setCreatedAt(LocalDateTime.now());

        return ticketPassRepository.save(pass);
    }

    // ============================================================
    // LAY DANH SACH VE DANG BAN
    // ============================================================

    public List<TicketPass> getAvailablePasses() {
        return ticketPassRepository.findByStatusOrderByCreatedAtDesc("AVAILABLE");
    }

    public List<TicketPass> getAllPasses() {
        return ticketPassRepository.findAllOrderByCreatedAtDesc();
    }

    public List<TicketPass> getSellerPasses(Long sellerId) {
        return ticketPassRepository.findBySellerIdWithDetails(sellerId);
    }

    public Optional<TicketPass> getPassById(Long id) {
        return ticketPassRepository.findById(id);
    }

    public TicketPass getPassByIdOrThrow(Long id) {
        return ticketPassRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));
    }

    // ============================================================
    // MUA TICKET PASS
    // ============================================================

    @Transactional
    public TicketPass buyPass(Long passId, Long buyerId) {
        TicketPass pass = getPassByIdOrThrow(passId);

        if (!"AVAILABLE".equals(pass.getStatus())) {
            throw new RuntimeException("Vé này không còn khả dụng");
        }

        User buyer = userRepository.findById(buyerId).orElse(null);
        if (buyer == null) throw new RuntimeException("Không tìm thấy người mua");

        if (pass.getSeller().getId().equals(buyerId)) {
            throw new RuntimeException("Bạn không thể mua vé của chính mình");
        }

        // Chuyen quyen so huu
        Booking booking = pass.getBooking();
        booking.setUser(buyer);
        booking.setCustomerName(buyer.getFullName());
        booking.setCustomerEmail(buyer.getEmail());
        booking.setCustomerPhone(buyer.getPhone());
        bookingRepository.save(booking);

        // Cap nhat trang thai pass
        pass.setBuyer(buyer);
        pass.setStatus("SOLD");
        return ticketPassRepository.save(pass);
    }

    // ============================================================
    // HUY TICKET PASS
    // ============================================================

    @Transactional
    public void cancelPass(Long passId, Long userId) {
        TicketPass pass = getPassByIdOrThrow(passId);

        if (!pass.getSeller().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy vé này");
        }

        if ("SOLD".equals(pass.getStatus())) {
            throw new RuntimeException("Vé đã được bán, không thể hủy");
        }

        pass.setStatus("CANCELLED");
        ticketPassRepository.save(pass);
    }

    // ============================================================
    // TIM KIEM
    // ============================================================

    public List<TicketPass> searchPasses(String keyword) {
        return ticketPassRepository.searchByKeyword(keyword);
    }

    public List<TicketPass> getPassesByShowtime(Long showtimeId) {
        return ticketPassRepository.findByShowtimeId(showtimeId);
    }

    // ============================================================
    // TRANG THAI HIEN THI
    // ============================================================

    public String getStatusLabel(String status) {
        return switch (status) {
            case "AVAILABLE" -> "Đang bán";
            case "LOCKED" -> "Đang khóa";
            case "SOLD" -> "Đã bán";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    public boolean canUserCancel(Long passId, Long userId) {
        TicketPass pass = getPassByIdOrThrow(passId);
        if (!"AVAILABLE".equals(pass.getStatus())) return false;
        return pass.getSeller().getId().equals(userId);
    }

    public boolean canUserBuy(Long passId, Long userId) {
        TicketPass pass = getPassByIdOrThrow(passId);
        if (!"AVAILABLE".equals(pass.getStatus())) return false;
        if (pass.getSeller() != null && pass.getSeller().getId().equals(userId)) return false;
        return true;
    }

    // Lay cac ve cua user de co the pass
    public List<Booking> getBookingsAvailableForPass(Long userId) {
        List<Booking> userBookings = bookingRepository.findByUserId(userId);
        return userBookings.stream()
            .filter(b -> "SUCCESS".equals(b.getStatus()))
            .filter(b -> b.getShowtime() != null)
            .filter(b -> b.getShowtime().getStartTime().isAfter(LocalDateTime.now()))
            .toList();
    }
}
