package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Booking;
import com.example.demoj2ee.model.DisputeMessage;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.BookingRepository;
import com.example.demoj2ee.repository.DisputeMessageRepository;
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

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads/evidence";

    @GetMapping("/dispute/{bookingId}")
    public String showDisputeRoom(@PathVariable Long bookingId, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<DisputeMessage> messages = disputeMessageRepository.findByBookingIdOrderBySentAtAsc(bookingId);

        model.addAttribute("messages", messages);
        model.addAttribute("bookingId", bookingId);

        return "dispute-chat";
    }

    @PostMapping("/api/dispute/send")
    public String sendMessage(@RequestParam("bookingId") Long bookingId,
                              @RequestParam("message") String messageContent,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return "redirect:/ticket-market";

        DisputeMessage msg = new DisputeMessage();
        msg.setBooking(booking);
        msg.setSender(loggedInUser);
        msg.setMessageContent(messageContent);
        msg.setSentAt(LocalDateTime.now());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIRECTORY);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String originalFilename = imageFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + extension;

                Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, newFileName);
                Files.write(fileNameAndPath, imageFile.getBytes());

                msg.setEvidenceImageUrl("/images/evidence/" + newFileName);

            } catch (IOException e) {
                System.err.println("Loi luu anh bang chung: " + e.getMessage());
            }
        }

        disputeMessageRepository.save(msg);
        return "redirect:/dispute/" + bookingId;
    }
}
