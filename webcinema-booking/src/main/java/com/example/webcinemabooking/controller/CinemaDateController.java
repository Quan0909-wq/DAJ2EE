package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.CinemaDate;
import com.example.webcinemabooking.model.MatchRequest; // THÊM IMPORT
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.CinemaDateRepository;
import com.example.webcinemabooking.repository.MatchRequestRepository; // THÊM IMPORT
import com.example.webcinemabooking.repository.MovieRepository;
import com.example.webcinemabooking.repository.ShowtimeRepository;
import com.example.webcinemabooking.repository.UserRepository;
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

    // GỌI THÊM KHO CHỨA THƯ TÌNH
    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @GetMapping("/pele-dating")
    public String showDatingPage(Model model, HttpSession session) {
        List<CinemaDate> datingList = cinemaDateRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        model.addAttribute("datingList", datingList);

        // TRUYỀN TÀI KHOẢN ĐANG ĐĂNG NHẬP RA HTML
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "dating";
    }

    // ----- HÀM 1: ĐĂNG TIN TÌM CẠ CỨNG -----
    @PostMapping("/pele-dating/post")
    public String createDatingPost(@RequestParam("bioMessage") String bioMessage,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("loggedInUser");
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Sếp ơi, phiên đăng nhập hết hạn hoặc chưa đăng nhập kìa!");
                return "redirect:/pele-dating";
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
            redirectAttributes.addFlashAttribute("successMessage", "💘 Tuyệt vời! Hồ sơ của sếp đã lên sóng, chờ người ấy cắn câu thôi!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi rùi sếp ơi: " + e.getMessage());
        }
        return "redirect:/pele-dating";
    }

    // ----- HÀM 2: XỬ LÝ KHÁCH BẤM "GHÉP ĐÔI" (GỬI THƯ TÌNH) -----
    @PostMapping("/pele-dating/request-match")
    public String requestMatch(@RequestParam("dateId") Long dateId,
                               @RequestParam("message") String message,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phải đăng nhập mới được đi tán gái nha sếp!");
            return "redirect:/login"; // Đá ra trang đăng nhập
        }

        CinemaDate targetDate = cinemaDateRepository.findById(dateId).orElse(null);
        if (targetDate == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thẻ này bị bay màu rồi sếp ơi!");
            return "redirect:/pele-dating";
        }

        // Chống tự kỷ: Không cho tự rep thẻ của chính mình
        if (targetDate.getHostUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đừng tự kỷ sếp ơi, không thể tự ghép đôi với bài của chính mình đâu! 😂");
            return "redirect:/pele-dating";
        }

        // Tạo thư tình lưu vào DB
        MatchRequest request = new MatchRequest();
        request.setCinemaDate(targetDate);
        request.setRequester(currentUser);
        request.setMessage(message);

        matchRequestRepository.save(request);

        redirectAttributes.addFlashAttribute("successMessage", "💌 Đã phóng thư tình thành công! Đợi người ta rep thôi!");
        return "redirect:/pele-dating";
    }
    // HÀM MỚI: XỬ LÝ TYM BẰNG AJAX (KHÔNG LOAD LẠI TRANG)
    @PostMapping("/pele-dating/toggle-like")
    @ResponseBody // <--- BÙA CHÚ QUAN TRỌNG: Ép Java trả về dữ liệu ngầm, cấm load lại trang
    public Map<String, Object> toggleLikeAjax(@RequestParam("dateId") Long dateId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("loggedInUser");

        // 1. Nếu chưa đăng nhập
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "not_logged_in");
            return response;
        }

        // 2. Nếu đã đăng nhập thì xử lý Tym
        CinemaDate date = cinemaDateRepository.findById(dateId).orElse(null);
        if (date != null) {
            boolean isLiked = date.isLikedBy(currentUser);
            if (isLiked) {
                date.getLikedByUsers().removeIf(u -> u.getId().equals(currentUser.getId())); // Hủy tym
                isLiked = false;
            } else {
                date.getLikedByUsers().add(currentUser); // Thêm tym
                isLiked = true;
            }
            cinemaDateRepository.save(date);

            // 3. Trả kết quả về cho HTML tự xử
            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", date.getLikedByUsers().size());
        }
        return response;
    }
    // -----------------------------------------------------------------
    // HÀM MỚI 1: CHỦ THỚT THẢ TYM BÌNH LUẬN
    @PostMapping("/pele-dating/like-comment")
    @ResponseBody
    public Map<String, Object> likeCommentAjax(@RequestParam("requestId") Long requestId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser != null) {
            MatchRequest req = matchRequestRepository.findById(requestId).orElse(null);
            // Chỉ chủ nhân bài viết mới được quyền thả tym bình luận
            if (req != null && req.getCinemaDate().getHostUser().getId().equals(currentUser.getId())) {
                req.setLikedByHost(!req.isLikedByHost()); // Bấm thì tym, bấm nữa thì hủy
                matchRequestRepository.save(req);
                response.put("success", true);
                response.put("isLiked", req.isLikedByHost());
            }
        }
        return response;
    }

    // HÀM MỚI 2: CHỦ THỚT TRẢ LỜI BÌNH LUẬN
    @PostMapping("/pele-dating/reply-comment")
    @ResponseBody
    public Map<String, Object> replyCommentAjax(@RequestParam("requestId") Long requestId,
                                                @RequestParam("replyText") String replyText,
                                                HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser != null) {
            MatchRequest req = matchRequestRepository.findById(requestId).orElse(null);
            // Chỉ chủ nhân bài viết mới được quyền rep
            if (req != null && req.getCinemaDate().getHostUser().getId().equals(currentUser.getId())) {
                req.setHostReply(replyText);
                matchRequestRepository.save(req);
                response.put("success", true);
                response.put("replyText", replyText);
            }
        }
        return response;
    }
    // HÀM MỚI 3: XỬ LÝ UPLOAD AVATAR
    @PostMapping("/pele-dating/update-avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser != null && !file.isEmpty()) {
            try {
                // Ma thuật: Biến bức ảnh thành 1 chuỗi ký tự siêu dài để nhét vào MySQL
                String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                String imageString = "data:" + file.getContentType() + ";base64," + base64Image;

                // Lưu vào DB
                currentUser.setAvatar(imageString);
                userRepository.save(currentUser);

                // Cập nhật lại Session để web nhận diện cái mặt sếp ngay lập tức
                session.setAttribute("loggedInUser", currentUser);

                redirectAttributes.addFlashAttribute("successMessage", "📸 Thay Avatar thành công! Đẹp trai/xinh gái xuất sắc!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tải ảnh rùi sếp: " + e.getMessage());
            }
        }
        return "redirect:/pele-dating";
    }
}