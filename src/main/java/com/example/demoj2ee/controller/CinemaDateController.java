package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.CinemaDate;
import com.example.demoj2ee.model.MatchRequest;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.CinemaDateRepository;
import com.example.demoj2ee.repository.MatchRequestRepository;
import com.example.demoj2ee.repository.MovieRepository;
import com.example.demoj2ee.repository.ShowtimeRepository;
import com.example.demoj2ee.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CinemaDateController {

    @Autowired
    private CinemaDateRepository cinemaDateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @GetMapping("/pele-dating")
    public String showDatingPage(Model model, HttpSession session) {
        List<CinemaDate> datingList = cinemaDateRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        model.addAttribute("datingList", datingList);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "dating";
    }

    @PostMapping("/pele-dating/post")
    public String createDatingPost(@RequestParam("bioMessage") String bioMessage,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("loggedInUser");
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phien dang nhap het han!");
                return "redirect:/pele-dating";
            }

            // Ràng buộc: phải hoàn thiện hồ sơ dating trước
            if (!currentUser.isDatingProfileComplete()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Hay hoan thien ho so dating truoc khi dang bai!");
                return "redirect:/settings/dating-profile";
            }

            CinemaDate newDate = new CinemaDate();
            newDate.setBioMessage(bioMessage);
            newDate.setStatus("PENDING");
            newDate.setHostUser(currentUser);

            if (!movieRepository.findAll().isEmpty()) {
                newDate.setMovie(movieRepository.findAll().get(0));
            }
            if (!showtimeRepository.findAll().isEmpty()) {
                newDate.setShowtime(showtimeRepository.findAll().get(0));
            }

            cinemaDateRepository.save(newDate);
            redirectAttributes.addFlashAttribute("successMessage", "Da dang tin tim ban thanh cong!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loi: " + e.getMessage());
        }
        return "redirect:/pele-dating";
    }

    @PostMapping("/pele-dating/request-match")
    public String requestMatch(@RequestParam("dateId") Long dateId,
                               @RequestParam("message") String message,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phai dang nhap moi duoc!");
            return "redirect:/login";
        }

        CinemaDate targetDate = cinemaDateRepository.findById(dateId).orElse(null);
        if (targetDate == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bai nay khong ton tai!");
            return "redirect:/pele-dating";
        }

        if (targetDate.getHostUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong the tu rep bai cua chinh minh!");
            return "redirect:/pele-dating";
        }

        MatchRequest request = new MatchRequest();
        request.setCinemaDate(targetDate);
        request.setRequester(currentUser);
        request.setMessage(message);

        matchRequestRepository.save(request);

        redirectAttributes.addFlashAttribute("successMessage", "Da gui loi moi thanh cong!");
        return "redirect:/pele-dating";
    }

    @PostMapping("/pele-dating/toggle-like")
    @ResponseBody
    public Map<String, Object> toggleLikeAjax(@RequestParam("dateId") Long dateId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "not_logged_in");
            return response;
        }

        CinemaDate date = cinemaDateRepository.findById(dateId).orElse(null);
        if (date != null) {
            boolean isLiked = date.isLikedBy(currentUser);
            if (isLiked) {
                date.getLikedByUsers().removeIf(u -> u.getId().equals(currentUser.getId()));
                isLiked = false;
            } else {
                date.getLikedByUsers().add(currentUser);
                isLiked = true;
            }
            cinemaDateRepository.save(date);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", date.getLikedByUsers().size());
        }
        return response;
    }

    @PostMapping("/pele-dating/like-comment")
    @ResponseBody
    public Map<String, Object> likeCommentAjax(@RequestParam("requestId") Long requestId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser != null) {
            MatchRequest req = matchRequestRepository.findById(requestId).orElse(null);
            if (req != null && req.getCinemaDate().getHostUser().getId().equals(currentUser.getId())) {
                req.setLikedByHost(!req.isLikedByHost());
                matchRequestRepository.save(req);
                response.put("success", true);
                response.put("isLiked", req.isLikedByHost());
            }
        }
        return response;
    }

    @PostMapping("/pele-dating/reply-comment")
    @ResponseBody
    public Map<String, Object> replyCommentAjax(@RequestParam("requestId") Long requestId,
                                                 @RequestParam("replyText") String replyText,
                                                 HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser != null) {
            MatchRequest req = matchRequestRepository.findById(requestId).orElse(null);
            if (req != null && req.getCinemaDate().getHostUser().getId().equals(currentUser.getId())) {
                req.setHostReply(replyText);
                matchRequestRepository.save(req);
                response.put("success", true);
                response.put("replyText", replyText);
            }
        }
        return response;
    }

    @PostMapping("/pele-dating/user-profile")
    @ResponseBody
    public Map<String, Object> getUserProfile(@RequestParam("userId") Long userId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("error", "Khong tim thay nguoi dung!");
            return response;
        }

        response.put("success", true);
        response.put("fullName", user.getFullName() != null ? user.getFullName() : user.getUsername());
        response.put("age", user.getAge());
        response.put("height", user.getHeight());
        response.put("hometown", user.getHometown() != null ? user.getHometown() : "—");
        response.put("relationshipStatus", getRelationshipLabel(user.getRelationshipStatus()));
        response.put("datingBio", user.getDatingBio() != null ? user.getDatingBio() : "—");
        response.put("datingAvatar", user.getDatingAvatar() != null ? user.getDatingAvatar() : user.getAvatar());
        response.put("datingPhoto1", user.getDatingPhoto1());
        response.put("datingPhoto2", user.getDatingPhoto2());
        response.put("datingPhoto3", user.getDatingPhoto3());
        return response;
    }

    private String getRelationshipLabel(String status) {
        if (status == null) return "—";
        return switch (status) {
            case "DOC_THAN" -> "Độc thân";
            case "HEN_HO" -> "Đang hẹn hò";
            case "PHUC_TAP" -> "Phức tạp";
            default -> status;
        };
    }

    @PostMapping("/pele-dating/update-avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser != null && !file.isEmpty()) {
            try {
                String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                String imageString = "data:" + file.getContentType() + ";base64," + base64Image;

                currentUser.setAvatar(imageString);
                userRepository.save(currentUser);
                session.setAttribute("loggedInUser", currentUser);

                redirectAttributes.addFlashAttribute("successMessage", "Da thay avatar thanh cong!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Loi tai anh: " + e.getMessage());
            }
        }
        return "redirect:/pele-dating";
    }
}
