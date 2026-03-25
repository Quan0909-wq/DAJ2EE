package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Booking;
import com.example.webcinemabooking.model.DisputeMessage;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.BookingRepository;
// Sếp nhớ tạo DisputeMessageRepository nhé!
import com.example.webcinemabooking.repository.DisputeMessageRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class DisputeController {

    @Autowired private DisputeMessageRepository disputeMessageRepository;
    @Autowired private BookingRepository bookingRepository;

    // Đường dẫn thư mục lưu ảnh (Nó sẽ tự tạo thư mục 'uploads' ở cùng chỗ với file pom.xml)
    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads/evidence";

    // 1. HÀM HIỂN THỊ PHÒNG CHAT
    @GetMapping("/dispute/{bookingId}")
    public String showDisputeRoom(@PathVariable Long bookingId, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // Tìm tất cả tin nhắn của vụ kiện này (Sếp nhớ thêm hàm findByBookingIdOrderBySentAtAsc vào Repository nhé)
        List<DisputeMessage> messages = disputeMessageRepository.findByBookingIdOrderBySentAtAsc(bookingId);

        model.addAttribute("messages", messages);
        model.addAttribute("bookingId", bookingId);

        return "dispute-chat";
    }

    // 2. HÀM XỬ LÝ GỬI TIN NHẮN & UPLOAD ẢNH
    @PostMapping("/api/dispute/send")
    public String sendMessage(@RequestParam("bookingId") Long bookingId,
                              @RequestParam("message") String messageContent,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return "redirect:/ticket-market"; // Quai lại chợ vé nếu lỗi

        DisputeMessage msg = new DisputeMessage();
        msg.setBooking(booking);
        msg.setSender(loggedInUser);
        msg.setMessageContent(messageContent);
        msg.setSentAt(LocalDateTime.now());

        // XỬ LÝ LƯU ẢNH BẰNG CHỨNG (NẾU CÓ)
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Tạo thư mục nếu chưa có
                File uploadDir = new File(UPLOAD_DIRECTORY);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                // Tạo tên file mới ngẫu nhiên (chống trùng tên)
                String originalFilename = imageFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + extension;

                // Lưu file vào ổ cứng
                Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, newFileName);
                Files.write(fileNameAndPath, imageFile.getBytes());

                // Lưu đường dẫn ảnh vào Database (Trỏ đến thư mục ảo /images/evidence/...)
                msg.setEvidenceImageUrl("/images/evidence/" + newFileName);

            } catch (IOException e) {
                System.err.println("Lỗi lưu ảnh bằng chứng: " + e.getMessage());
            }
        }

        // Lưu vào DB
        disputeMessageRepository.save(msg);

        // Đá người dùng quay lại đúng cái phòng chat đó để xem tin nhắn vừa gửi
        return "redirect:/dispute/" + bookingId;
    }
}