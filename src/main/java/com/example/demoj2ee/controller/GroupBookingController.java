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

import java.util.*;

@Controller
public class GroupBookingController {

    @Autowired private GroupBookingService groupBookingService;
    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private GroupBookingRepository groupBookingRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private GroupPaymentRepository groupPaymentRepository;
    @Autowired private BookingRepository bookingRepository;

    // ============================================================
    // TRANG CHINH - TAO / THAM GIA PHONG
    // ============================================================

    @GetMapping("/group")
    public String groupBookingPage(Model model) {
        model.addAttribute("showtimes", showtimeRepository.findAll());
        return "group-booking";
    }

    // ============================================================
    // TAO PHONG NHOM MOI
    // ============================================================

    @PostMapping("/group/create")
    public String createGroup(@RequestParam Long showtimeId,
                              @RequestParam String creatorName,
                              @RequestParam(required = false) String creatorEmail,
                              @RequestParam(required = false) String creatorPhone,
                              HttpSession session,
                              RedirectAttributes ra) {
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser != null) {
                creatorEmail = loggedInUser.getEmail();
                creatorPhone = loggedInUser.getPhone();
                if (creatorName == null || creatorName.isEmpty()) {
                    creatorName = loggedInUser.getFullName();
                }
            }
            if (creatorEmail == null || creatorEmail.isEmpty()) creatorEmail = "guest_" + System.currentTimeMillis() + "@pele.com";
            if (creatorPhone == null || creatorPhone.isEmpty()) creatorPhone = "0000000000";

            GroupBooking group = groupBookingService.createGroup(showtimeId, creatorName, creatorEmail, creatorPhone);
            ra.addFlashAttribute("roomCode", group.getRoomCode());
            return "redirect:/group/" + group.getRoomCode();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/group";
        }
    }

    // ============================================================
    // TRANG LOBBY - XEM PHONG VA MOI THANH VIEN
    // ============================================================

    @GetMapping("/group/{roomCode}")
    public String groupLobby(@PathVariable String roomCode, Model model, HttpSession session) {
        GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
        if (group == null) return "redirect:/group?error=Không+tìm+thấy+phòng";

        if (group.getStatus() == 3) return "redirect:/group?error=Phòng+đã+hết+hạn";
        if (group.getStatus() == 2) return "redirect:/group/" + roomCode + "/success";

        List<GroupMember> members = groupBookingService.getMembers(group.getId());
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Tim thanh vien hien tai
        GroupMember currentMember = null;
        if (loggedInUser != null) {
            currentMember = members.stream()
                .filter(m -> m.getEmail().equals(loggedInUser.getEmail()))
                .findFirst().orElse(null);
        }
        if (currentMember == null) {
            for (GroupMember m : members) {
                if (m.isCreator()) {
                    currentMember = m;
                    break;
                }
            }
        }

        Map<String, Object> status = groupBookingService.getGroupStatus(group.getId());

        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("currentMember", currentMember);
        model.addAttribute("statusData", status);
        model.addAttribute("showtime", group.getShowtime());

        return "group-lobby";
    }

    // ============================================================
    // MOI THANH VIEN
    // ============================================================

    @PostMapping("/group/{roomCode}/invite")
    @ResponseBody
    public Map<String, Object> inviteMember(@PathVariable String roomCode,
                                            @RequestParam String name,
                                            @RequestParam String email,
                                            @RequestParam(required = false) String phone) {
        Map<String, Object> result = new HashMap<>();
        try {
            GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
            if (group == null) throw new RuntimeException("Phòng không tồn tại");

            GroupMember member = groupBookingService.addMember(group.getId(), name, email, phone);
            result.put("success", true);
            result.put("memberId", member.getId());
            result.put("name", member.getName());
            result.put("email", member.getEmail());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // ============================================================
    // THAM GIA PHONG - TIM THANH VIEN
    // ============================================================

    @GetMapping("/group/join")
    public String joinGroup(@RequestParam String roomCode,
                           @RequestParam(required = false) String email,
                           RedirectAttributes ra) {
        GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
        if (group == null) {
            ra.addFlashAttribute("error", "Không tìm thấy phòng");
            return "redirect:/group";
        }
        if (group.getStatus() == 3) {
            ra.addFlashAttribute("error", "Phòng đã hết hạn");
            return "redirect:/group";
        }

        if (email != null && !email.isEmpty()) {
            Optional<GroupMember> member = groupMemberRepository.findByGroupBookingIdAndEmail(group.getId(), email);
            if (member.isPresent()) {
                if (member.get().getJoinStatus() == 0) {
                    member.get().setJoinStatus(1);
                    groupMemberRepository.save(member.get());
                }
                return "redirect:/group/" + roomCode + "?memberId=" + member.get().getId();
            }
        }
        return "redirect:/group/" + roomCode;
    }

    // ============================================================
    // THAM GIA PHONG (TUY CHON - CHON GHE)
    // ============================================================

    @GetMapping("/group/{roomCode}/join")
    public String joinAsMember(@PathVariable String roomCode,
                               @RequestParam Long memberId,
                               RedirectAttributes ra) {
        GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
        if (group == null) return "redirect:/group?error=Không+tìm+thấy+phòng";

        GroupMember member = groupMemberRepository.findById(memberId).orElse(null);
        if (member != null && member.getJoinStatus() == 0) {
            member.setJoinStatus(1);
            groupMemberRepository.save(member);
        }
        return "redirect:/group/" + roomCode;
    }

    // ============================================================
    // TRANG CHON GHE (SEAT MAP)
    // ============================================================

    @GetMapping("/group/{roomCode}/seats")
    public String selectSeats(@PathVariable String roomCode,
                               @RequestParam(required = false) Long memberId,
                               Model model,
                               HttpSession session) {
        GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
        if (group == null) return "redirect:/group";

        if (group.getStatus() == 3) return "redirect:/group?error=Phòng+đã+hết+hạn";
        if (group.getStatus() == 2) return "redirect:/group/" + roomCode + "/success";

        Showtime showtime = group.getShowtime();
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Tim member
        GroupMember currentMember = null;
        if (memberId != null) {
            currentMember = groupMemberRepository.findById(memberId).orElse(null);
        } else if (loggedInUser != null) {
            currentMember = groupMemberRepository.findByGroupBookingIdAndEmail(group.getId(), loggedInUser.getEmail()).orElse(null);
        }
        if (currentMember == null) {
            List<GroupMember> members = groupMemberRepository.findByGroupBookingId(group.getId());
            for (GroupMember m : members) {
                if (m.isCreator()) {
                    currentMember = m;
                    break;
                }
            }
        }

        // Lay danh sach ghe da ban
        Set<String> occupiedSeats = groupBookingService.getOccupiedSeats(showtime.getId(), roomCode);

        // Lay ghe da chon trong phong
        Set<String> groupSeats = new HashSet<>();
        List<GroupMember> allMembers = groupMemberRepository.findByGroupBookingId(group.getId());
        for (GroupMember m : allMembers) {
            if (m.getId() != null && currentMember != null && m.getId().equals(currentMember.getId())) continue;
            if (m.getSeats() != null && !m.getSeats().isEmpty()) {
                for (String s : m.getSeats().split(",")) {
                    groupSeats.add(s.trim());
                }
            }
        }

        Map<String, Object> status = groupBookingService.getGroupStatus(group.getId());

        model.addAttribute("group", group);
        model.addAttribute("showtime", showtime);
        model.addAttribute("rows", 10);
        model.addAttribute("cols", 10);
        model.addAttribute("products", Collections.emptyList());
        model.addAttribute("currentMember", currentMember);
        model.addAttribute("occupiedSeats", String.join(",", occupiedSeats));
        model.addAttribute("groupSeats", String.join(",", groupSeats));
        model.addAttribute("statusData", status);
        model.addAttribute("isGroupMode", true);

        return "group-seat";
    }

    @PostMapping("/group/{roomCode}/seats")
    @ResponseBody
    public Map<String, Object> saveSeats(@PathVariable String roomCode,
                                         @RequestParam Long memberId,
                                         @RequestParam String seats) {
        Map<String, Object> result = new HashMap<>();
        try {
            GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
            if (group == null) throw new RuntimeException("Phòng không tồn tại");

            groupBookingService.selectSeats(memberId, seats);

            Map<String, Object> status = groupBookingService.getGroupStatus(group.getId());
            result.put("success", true);
            result.put("status", status);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // ============================================================
    // TRANG THANH TOAN
    // ============================================================

    @GetMapping("/group/{roomCode}/payment")
    public String groupPayment(@PathVariable String roomCode,
                               @RequestParam(required = false) Long memberId,
                               Model model,
                               HttpSession session) {
        GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
        if (group == null) return "redirect:/group";

        if (group.getStatus() == 3) return "redirect:/group?error=Phòng+đã+hết+hạn";
        if (group.getStatus() == 2) return "redirect:/group/" + roomCode + "/success";

        if (group.getAllSeats() == null || group.getAllSeats().isEmpty()) {
            return "redirect:/group/" + roomCode + "?error=Chưa+chọn+ghế";
        }

        // Setup payment neu chua co
        List<GroupPayment> existingPayments = groupPaymentRepository.findByGroupBookingId(group.getId());
        if (existingPayments.isEmpty()) {
            try {
                groupBookingService.setupSplitPayment(group.getId(), 0);
            } catch (Exception e) {
                return "redirect:/group/" + roomCode + "?error=" + e.getMessage();
            }
        }

        List<GroupMember> members = groupMemberRepository.findByGroupBookingId(group.getId());
        List<GroupPayment> payments = groupPaymentRepository.findByGroupBookingId(group.getId());
        Map<String, Object> status = groupBookingService.getGroupStatus(group.getId());

        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("payments", payments);
        model.addAttribute("statusData", status);
        model.addAttribute("showtime", group.getShowtime());

        return "group-payment";
    }

    // ============================================================
    // THANH TOAN MOT NGUOI
    // ============================================================

    @PostMapping("/group/{roomCode}/pay-single")
    public String paySingle(@PathVariable String roomCode,
                            @RequestParam String customerName,
                            @RequestParam(required = false) String customerEmail,
                            @RequestParam(required = false) String customerPhone,
                            RedirectAttributes ra) {
        try {
            GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
            if (group == null) return "redirect:/group";

            if (customerEmail == null || customerEmail.isEmpty()) customerEmail = "group@" + roomCode + ".pele";
            if (customerPhone == null || customerPhone.isEmpty()) customerPhone = "0000000000";

            Booking booking = groupBookingService.paySingle(group.getId(), customerName, customerEmail, customerPhone);
            ra.addFlashAttribute("bookingId", booking.getId());
            ra.addFlashAttribute("roomCode", roomCode);
            return "redirect:/group/" + roomCode + "/success";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/group/" + roomCode + "/payment";
        }
    }

    // ============================================================
    // CHON CHE DO THANH TOAN CHIA DEU
    // ============================================================

    @PostMapping("/group/{roomCode}/setup-split")
    public String setupSplitPayment(@PathVariable String roomCode,
                                    @RequestParam int paymentMode,
                                    RedirectAttributes ra) {
        try {
            GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
            if (group == null) return "redirect:/group";

            groupBookingService.setupSplitPayment(group.getId(), paymentMode);
            return "redirect:/group/" + roomCode + "/payment";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/group/" + roomCode;
        }
    }

    // ============================================================
    // XAC NHAN THANH TOAN CHIA DEU (MO PHONG)
    // ============================================================

    @PostMapping("/group/{roomCode}/pay-split")
    @ResponseBody
    public Map<String, Object> paySplit(@PathVariable String roomCode,
                                        @RequestParam Long paymentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
            if (group == null) throw new RuntimeException("Phòng không tồn tại");

            GroupPayment payment = groupBookingService.confirmPayment(paymentId);
            Map<String, Object> status = groupBookingService.getGroupStatus(group.getId());

            result.put("success", true);
            result.put("payment", payment);
            result.put("status", status);

            Object st = status.get("status");
            if (st instanceof Number && ((Number) st).intValue() == 2) {
                result.put("allPaid", true);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // ============================================================
    // KIEM TRA TRANG THAI (AJAX POLLING)
    // ============================================================

    @GetMapping("/group/{roomCode}/status")
    @ResponseBody
    public Map<String, Object> getStatus(@PathVariable String roomCode) {
        GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
        if (group == null) return Map.of("error", "Not found");

        Map<String, Object> status = groupBookingService.getGroupStatus(group.getId());
        List<GroupPayment> payments = groupPaymentRepository.findByGroupBookingId(group.getId());

        status.put("payments", payments);
        int confirmed = 0;
        Object cc = status.get("confirmedCount");
        if (cc instanceof Number) {
            confirmed = ((Number) cc).intValue();
        }
        status.put("allPaid", confirmed == payments.size() && !payments.isEmpty());

        return status;
    }

    // ============================================================
    // TRANG THANH CONG
    // ============================================================

    @GetMapping("/group/{roomCode}/success")
    public String groupSuccess(@PathVariable String roomCode,
                               @RequestParam(required = false) Long bookingId,
                               Model model) {
        GroupBooking group = groupBookingService.getGroupByRoomCode(roomCode);
        if (group == null) return "redirect:/group";

        List<GroupMember> members = groupMemberRepository.findByGroupBookingId(group.getId());

        // Tim booking gan nhat
        Booking booking = null;
        if (bookingId != null) {
            booking = bookingRepository.findById(bookingId).orElse(null);
        } else {
            List<Booking> bookings = bookingRepository.findAll();
            for (int i = bookings.size() - 1; i >= 0; i--) {
                if (bookings.get(i).getCustomerEmail() != null &&
                    bookings.get(i).getCustomerEmail().contains(roomCode)) {
                    booking = bookings.get(i);
                    break;
                }
            }
        }

        Map<String, Object> status = groupBookingService.getGroupStatus(group.getId());

        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("booking", booking);
        model.addAttribute("statusData", status);
        model.addAttribute("showtime", group.getShowtime());

        return "group-success";
    }
}
