package com.example.demoj2ee.service;

import com.example.demoj2ee.model.*;
import com.example.demoj2ee.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GroupBookingService {

    @Autowired private GroupBookingRepository groupBookingRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private GroupPaymentRepository groupPaymentRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ShowtimeRepository showtimeRepository;

    private static final Random random = new Random();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    // TAO PHONG NHOM
    // ============================================================

    @Transactional
    public GroupBooking createGroup(Long showtimeId, String creatorName, String creatorEmail, String creatorPhone, User creatorUser) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) throw new RuntimeException("Suất chiếu không tồn tại");

        GroupBooking group = new GroupBooking();
        group.setRoomCode(generateRoomCode());
        group.setShowtime(showtime);
        group.setCreator(creatorUser);
        group.setStatus(0);
        group.setCreatedAt(LocalDateTime.now());
        group.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        group.setPaymentMode(0);
        group.setConfirmedCount(0);
        group.setAllSeats("");
        group.setOriginalAmount(0);
        group.setDiscountPercent(0);
        group.setFinalAmount(0);

        GroupBooking saved = groupBookingRepository.save(group);

        // Tao thanh vien la nguoi tao
        GroupMember creator = new GroupMember();
        creator.setGroupBooking(saved);
        creator.setName(creatorName);
        creator.setEmail(creatorEmail);
        creator.setPhone(creatorPhone);
        creator.setCreator(true);
        creator.setJoinStatus(1);
        creator.setSeats("");
        groupMemberRepository.save(creator);

        return saved;
    }

    // ============================================================
    // THEM THANH VIEN
    // ============================================================

    @Transactional
    public GroupMember addMember(Long groupId, String name, String email, String phone) {
        GroupBooking group = groupBookingRepository.findById(groupId).orElse(null);
        if (group == null) throw new RuntimeException("Phòng không tồn tại");
        if (group.getStatus() == 3) throw new RuntimeException("Phòng đã hết hạn");
        if (group.getStatus() == 2) throw new RuntimeException("Phòng đã thanh toán");

        // Kiem tra email da ton tai chua
        Optional<GroupMember> existing = groupMemberRepository.findByGroupBookingIdAndEmail(groupId, email);
        if (existing.isPresent()) {
            return existing.get();
        }

        GroupMember member = new GroupMember();
        member.setGroupBooking(group);
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        member.setCreator(false);
        member.setJoinStatus(0);
        member.setSeats("");
        return groupMemberRepository.save(member);
    }

    // ============================================================
    // CHON GHE
    // ============================================================

    @Transactional
    public GroupMember selectSeats(Long memberId, String seats) {
        GroupMember member = groupMemberRepository.findById(memberId).orElse(null);
        if (member == null) throw new RuntimeException("Thành viên không tồn tại");

        GroupBooking group = member.getGroupBooking();
        if (group.getStatus() == 3) throw new RuntimeException("Phòng đã hết hạn");

        // Kiem tra trung ghe
        String newSeatsStr = seats != null ? seats.trim() : "";
        if (!newSeatsStr.isEmpty()) {
            Set<String> allSeats = getAllSelectedSeats(group.getId());
            String[] newSeatArr = newSeatsStr.split(",");
            for (String seat : newSeatArr) {
                String trimmed = seat.trim();
                if (allSeats.contains(trimmed)) {
                    throw new RuntimeException("Ghế " + trimmed + " đã được chọn bởi thành viên khác!");
                }
                allSeats.add(trimmed);
            }
        }

        member.setSeats(newSeatsStr);
        member.setJoinStatus(newSeatsStr.isEmpty() ? 1 : 2);
        groupMemberRepository.save(member);

        // Cap nhat tong ghe cho group
        updateGroupSeats(group.getId());

        // Chuyen trang thai sang dang chon ghe
        group.setStatus(1);
        groupBookingRepository.save(group);

        return member;
    }

    private Set<String> getAllSelectedSeats(Long groupId) {
        Set<String> seats = new HashSet<>();
        List<GroupMember> members = groupMemberRepository.findByGroupBookingId(groupId);
        for (GroupMember m : members) {
            if (m.getSeats() != null && !m.getSeats().isEmpty()) {
                for (String s : m.getSeats().split(",")) {
                    seats.add(s.trim());
                }
            }
        }
        return seats;
    }

    private void updateGroupSeats(Long groupId) {
        GroupBooking group = groupBookingRepository.findById(groupId).orElse(null);
        if (group == null) return;

        List<GroupMember> members = groupMemberRepository.findByGroupBookingId(groupId);
        StringBuilder allSeats = new StringBuilder();
        double totalAmount = 0;

        for (GroupMember m : members) {
            if (m.getSeats() != null && !m.getSeats().isEmpty()) {
                if (allSeats.length() > 0) allSeats.append(",");
                allSeats.append(m.getSeats());
                totalAmount += m.getSeatCount() * group.getShowtime().getPrice();
            }
        }

        group.setAllSeats(allSeats.toString());
        group.setOriginalAmount(totalAmount);
        int seatCount = group.getTotalSeats();
        group.setDiscountPercent(GroupBooking.getDiscountBySeats(seatCount));
        group.setFinalAmount(totalAmount * (1 - group.getDiscountPercent()));

        groupBookingRepository.save(group);
    }

    // ============================================================
    // THANH TOAN - MOT NGUOI TRA
    // ============================================================

    @Transactional
    public Booking paySingle(Long groupId, String customerName, String customerEmail, String customerPhone) {
        GroupBooking group = groupBookingRepository.findById(groupId).orElse(null);
        if (group == null) throw new RuntimeException("Phòng không tồn tại");
        if (group.getStatus() == 3) throw new RuntimeException("Phòng đã hết hạn");
        if (group.getAllSeats() == null || group.getAllSeats().isEmpty()) {
            throw new RuntimeException("Chưa có ghế nào được chọn");
        }

        // Tao Booking
        Booking booking = new Booking();
        booking.setShowtime(group.getShowtime());
        booking.setSeatNumbers(group.getAllSeats());
        booking.setTotalAmount(group.getFinalAmount());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("SUCCESS");
        booking.setCustomerName(customerName);
        booking.setCustomerEmail(customerEmail);
        booking.setCustomerPhone(customerPhone);

        Booking saved = bookingRepository.save(booking);

        // Cap nhat trang thai group
        group.setStatus(2);
        group.setConfirmedCount(1);
        groupBookingRepository.save(group);

        return saved;
    }

    // ============================================================
    // THANH TOAN - CHIA DEU
    // ============================================================

    @Transactional
    public void setupSplitPayment(Long groupId, int paymentMode) {
        GroupBooking group = groupBookingRepository.findById(groupId).orElse(null);
        if (group == null) throw new RuntimeException("Phòng không tồn tại");

        group.setPaymentMode(paymentMode);
        groupBookingRepository.save(group);

        // Xoa cac payment cu (neu co)
        List<GroupPayment> existingPayments = groupPaymentRepository.findByGroupBookingId(groupId);
        groupPaymentRepository.deleteAll(existingPayments);

        // Tao payment cho tung thanh vien
        List<GroupMember> members = groupMemberRepository.findByGroupBookingId(groupId);
        int memberWithSeats = 0;
        for (GroupMember m : members) {
            if (m.getSeatCount() > 0) memberWithSeats++;
        }

        if (memberWithSeats == 0) throw new RuntimeException("Chưa có ghế nào được chọn");

        double pricePerSeat = group.getShowtime().getPrice();
        double amountPerPerson = (group.getFinalAmount() / memberWithSeats);

        for (GroupMember m : members) {
            if (m.getSeatCount() == 0) continue;

            GroupPayment payment = new GroupPayment();
            payment.setGroupBooking(group);
            payment.setMember(m);
            payment.setAmount(amountPerPerson * m.getSeatCount());
            payment.setStatus("PENDING");
            payment.setPaymentMethod("VNPAY_MOCK");
            payment.setQrData(generateMockQRData(m.getEmail(), payment.getAmount()));
            groupPaymentRepository.save(payment);
        }
    }

    @Transactional
    public GroupPayment confirmPayment(Long paymentId) {
        GroupPayment payment = groupPaymentRepository.findById(paymentId).orElse(null);
        if (payment == null) throw new RuntimeException("Payment not found");

        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());
        groupPaymentRepository.save(payment);

        // Kiem tra tat ca da thanh toan chua
        GroupBooking group = payment.getGroupBooking();
        List<GroupPayment> allPayments = groupPaymentRepository.findByGroupBookingId(group.getId());
        int paidCount = 0;
        for (GroupPayment p : allPayments) {
            if ("PAID".equals(p.getStatus())) paidCount++;
        }

        group.setConfirmedCount(paidCount);

        // Tao Booking neu tat ca da thanh toan
        if (paidCount == allPayments.size()) {
            Booking booking = new Booking();
            booking.setShowtime(group.getShowtime());
            booking.setSeatNumbers(group.getAllSeats());
            booking.setTotalAmount(group.getFinalAmount());
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus("SUCCESS");
            booking.setCustomerName("Group Booking - " + group.getRoomCode());
            booking.setCustomerEmail("group@" + group.getRoomCode() + ".pele");
            booking.setCustomerPhone("");
            bookingRepository.save(booking);

            group.setStatus(2);
        }

        groupBookingRepository.save(group);
        return payment;
    }

    // ============================================================
    // LAY THONG TIN
    // ============================================================

    public GroupBooking getGroupByRoomCode(String roomCode) {
        return groupBookingRepository.findByRoomCode(roomCode).orElse(null);
    }

    public List<GroupMember> getMembers(Long groupId) {
        return groupMemberRepository.findByGroupBookingId(groupId);
    }

    public List<GroupPayment> getPayments(Long groupId) {
        return groupPaymentRepository.findByGroupBookingId(groupId);
    }

    public Map<String, Object> getGroupStatus(Long groupId) {
        GroupBooking group = groupBookingRepository.findById(groupId).orElse(null);
        if (group == null) return null;

        Map<String, Object> status = new HashMap<>();
        status.put("status", group.getStatus());
        status.put("totalSeats", group.getTotalSeats());
        status.put("originalAmount", group.getOriginalAmount());
        status.put("discountPercent", group.getDiscountPercent());
        status.put("discountText", GroupBooking.getDiscountText(group.getTotalSeats()));
        status.put("finalAmount", group.getFinalAmount());
        status.put("confirmedCount", group.getConfirmedCount());
        status.put("paymentMode", group.getPaymentMode());
        status.put("expiresAt", group.getExpiresAt());
        status.put("remainingSeconds", getRemainingSeconds(group));

        List<GroupMember> members = groupMemberRepository.findByGroupBookingId(groupId);
        status.put("memberCount", members.size());
        status.put("seatedCount", members.stream().filter(m -> m.getJoinStatus() == 2).count());

        return status;
    }

    // ============================================================
    // SCHEDULED - DONG PHONG HET HAN
    // ============================================================

    @Scheduled(fixedRate = 60000) // Chay moi 1 phut
    @Transactional
    public void cleanupExpiredRooms() {
        List<GroupBooking> expired = groupBookingRepository.findByExpiresAtBefore(LocalDateTime.now());
        for (GroupBooking group : expired) {
            if (group.getStatus() != 2) { // Neu chua thanh toan
                group.setStatus(3); // Danh dau het han
                groupBookingRepository.save(group);
            }
        }
    }

    // ============================================================
    // UTILITIES
    // ============================================================

    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        // Dam bao khong trung
        if (groupBookingRepository.findByRoomCode(code.toString()).isPresent()) {
            return generateRoomCode();
        }
        return code.toString();
    }

    private String generateMockQRData(String email, double amount) {
        return String.format(
            "PELE|%s|%.0f|%s|VNPAY",
            email,
            amount,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );
    }

    public long getRemainingSeconds(GroupBooking group) {
        if (group.getExpiresAt() == null) return 0;
        long seconds = java.time.Duration.between(LocalDateTime.now(), group.getExpiresAt()).getSeconds();
        return Math.max(0, seconds);
    }

    // Lay danh sach ghe da ban (cho seat map)
    public Set<String> getOccupiedSeats(Long showtimeId, String excludeRoomCode) {
        Set<String> seats = new HashSet<>();

        // Lay tu bookings thong thuong
        List<Booking> bookings = bookingRepository.findByShowtimeId(showtimeId);
        for (Booking b : bookings) {
            if (b.getSeatNumbers() != null) {
                for (String s : b.getSeatNumbers().split(",")) {
                    seats.add(s.trim());
                }
            }
        }

        // Lay tu group bookings chua thanh toan
        List<GroupBooking> groups = groupBookingRepository.findByShowtimeIdAndStatus(showtimeId, 1);
        for (GroupBooking g : groups) {
            if (excludeRoomCode != null && g.getRoomCode().equals(excludeRoomCode)) continue;
            if (g.getAllSeats() != null && !g.getAllSeats().isEmpty()) {
                for (String s : g.getAllSeats().split(",")) {
                    seats.add(s.trim());
                }
            }
        }

        return seats;
    }
}
