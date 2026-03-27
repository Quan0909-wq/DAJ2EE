package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.*;
import com.example.demoj2ee.repository.*;
import com.example.demoj2ee.service.DatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class DatingController {

    @Autowired
    private DatingService datingService;

    @Autowired
    private DatingProfileRepository datingProfileRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private UserRepository userRepository;

    /** In-memory feed for "tin tìm bạn" (newest first). */
    private static final List<DatingMoviePost> DATING_POSTS = Collections.synchronizedList(new ArrayList<>());
    private static final AtomicLong DATING_POST_ID = new AtomicLong(1);

    private User getUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // ============ DATING SETTINGS / PROFILE (chỉ user đăng nhập — không có userId trên URL) ============

    @GetMapping("/dating-profile")
    public String datingProfile(HttpSession session, Model model) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";
        DatingProfile profile = datingService.getOrCreateProfile(user);
        model.addAttribute("profile", profile);
        model.addAttribute("user", user);
        return "dating-profile";
    }

    @PostMapping("/dating-profile/save")
    public String saveProfile(HttpSession session,
                             @RequestParam(required = false) String displayName,
                             @RequestParam(required = false) Integer age,
                             @RequestParam(required = false) Double height,
                             @RequestParam(required = false) String hometown,
                             @RequestParam(required = false) String bio,
                             @RequestParam(required = false) String maritalStatus,
                             @RequestParam(required = false) MultipartFile avatarFile,
                             @RequestParam(required = false) MultipartFile photoFile1,
                             @RequestParam(required = false) MultipartFile photoFile2,
                             @RequestParam(required = false) MultipartFile photoFile3,
                             @RequestParam(required = false) MultipartFile photoFile4,
                             @RequestParam(required = false) Boolean keepAvatar,
                             @RequestParam(required = false) Boolean keepPhoto1,
                             @RequestParam(required = false) Boolean keepPhoto2,
                             @RequestParam(required = false) Boolean keepPhoto3,
                             @RequestParam(required = false) Boolean keepPhoto4,
                             @RequestParam(required = false) Boolean isActive,
                             RedirectAttributes ra) {

        User user = getUser(session);
        if (user == null) return "redirect:/login";

        List<String> errors = new ArrayList<>();
        if (displayName == null || displayName.isBlank()) errors.add("Tên hiển thị không được để trống.");
        if (displayName != null && displayName.length() > 50) errors.add("Tên hiển thị không được quá 50 ký tự.");
        if (age == null) errors.add("Tuổi không được để trống.");
        else if (age < 18) errors.add("Bạn phải từ 18 tuổi trở lên.");
        else if (age > 99) errors.add("Tuổi không được vượt quá 99.");
        if (height == null) errors.add("Chiều cao không được để trống.");
        else if (height < 100) errors.add("Chiều cao phải từ 100cm trở lên.");
        else if (height > 250) errors.add("Chiều cao không được vượt quá 250cm.");
        if (hometown == null || hometown.isBlank()) errors.add("Quê quán không được để trống.");
        if (maritalStatus == null || maritalStatus.isBlank()) errors.add("Tình trạng hôn nhân không được để trống.");

        DatingProfile existing = datingService.getOrCreateProfile(user);
        boolean hasNewAvatar = avatarFile != null && !avatarFile.isEmpty();
        boolean hasNewPhoto1 = photoFile1 != null && !photoFile1.isEmpty();
        boolean hasNewPhoto2 = photoFile2 != null && !photoFile2.isEmpty();
        boolean hasNewPhoto3 = photoFile3 != null && !photoFile3.isEmpty();
        boolean hasNewPhoto4 = photoFile4 != null && !photoFile4.isEmpty();

        if (!hasNewAvatar && (keepAvatar == null || !keepAvatar)) {
            if (existing.getAvatarUrl() == null || existing.getAvatarUrl().isBlank()) {
                errors.add("Ảnh đại diện là bắt buộc.");
            }
        }

        // Validate file types
        if (hasNewAvatar && !isValidImage(avatarFile)) errors.add("Ảnh đại diện phải là file ảnh (jpg, png, gif, webp).");
        if (hasNewPhoto1 && !isValidImage(photoFile1)) errors.add("Photo 1 phải là file ảnh.");
        if (hasNewPhoto2 && !isValidImage(photoFile2)) errors.add("Photo 2 phải là file ảnh.");
        if (hasNewPhoto3 && !isValidImage(photoFile3)) errors.add("Photo 3 phải là file ảnh.");
        if (hasNewPhoto4 && !isValidImage(photoFile4)) errors.add("Photo 4 phải là file ảnh.");

        if (!errors.isEmpty()) {
            ra.addFlashAttribute("errors", errors);
            return "redirect:/dating-profile";
        }

        DatingProfile profile = datingService.getOrCreateProfile(user);
        profile.setDisplayName(displayName.trim());
        profile.setAge(age);
        profile.setHeight(height);
        profile.setHometown(hometown.trim());
        profile.setBio(bio != null ? bio.trim() : null);
        profile.setMaritalStatus(maritalStatus);
        profile.setActive(isActive != null && isActive);

        // Handle avatar upload
        if (hasNewAvatar) {
            profile.setAvatarUrl(fileToBase64(avatarFile));
        }
        // Handle photo uploads
        if (hasNewPhoto1) profile.setPhoto1(fileToBase64(photoFile1));
        if (hasNewPhoto2) profile.setPhoto2(fileToBase64(photoFile2));
        if (hasNewPhoto3) profile.setPhoto3(fileToBase64(photoFile3));
        if (hasNewPhoto4) profile.setPhoto4(fileToBase64(photoFile4));

        datingService.saveProfile(profile);
        ra.addFlashAttribute("success", "Hồ sơ dating đã được lưu thành công!");
        return "redirect:/dating-profile";
    }

    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;
        return contentType.startsWith("image/") &&
            (contentType.contains("jpeg") || contentType.contains("png") ||
             contentType.contains("gif") || contentType.contains("webp") ||
             contentType.contains("bmp"));
    }

    private String fileToBase64(MultipartFile file) {
        try {
            return "data:" + file.getContentType() + ";base64," +
                   Base64.getEncoder().encodeToString(file.getBytes());
        } catch (Exception e) {
            return null;
        }
    }

    // ============ PELE DATING MAIN PAGE ============

    @GetMapping("/pele-dating")
    public String peleDating(HttpSession session, Model model) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        DatingProfile myProfile = datingProfileRepository.findByUser(user).orElse(null);
        model.addAttribute("myProfile", myProfile);

        List<DatingMatch> userMatches = datingService.getUserMatches(user.getId());
        List<DatingRequest> pendingRequests = datingService.getPendingRequests(user.getId());

        model.addAttribute("matches", userMatches);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("post", new DatingPostData());
        model.addAttribute("loggedInUser", user);
        synchronized (DATING_POSTS) {
            model.addAttribute("datingPosts", new ArrayList<>(DATING_POSTS));
        }

        return "pele-dating";
    }

    /**
     * Trình duyệt chỉ hiển thị URL này khi dùng GET (F5, mở tab, bookmark).
     * Form đăng tin phải gửi POST; GET được chuyển về trang dating để tránh 404.
     */
    @GetMapping("/pele-dating/create-post")
    public String createPostPageGet() {
        return "redirect:/pele-dating";
    }

    @PostMapping("/pele-dating/create-post")
    public String createPost(HttpSession session,
                             @RequestParam Long movieId,
                             @RequestParam Long showtimeId,
                             @RequestParam String dateTime,
                             @RequestParam int peopleCount,
                             @RequestParam String seatPreference,
                             @RequestParam String theater,
                             RedirectAttributes ra) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        DatingProfile profile = datingProfileRepository.findByUser(user).orElse(null);
        if (profile == null || !profile.isProfileComplete()) {
            ra.addFlashAttribute("postError", "Vui lòng hoàn thành hồ sơ dating trước khi đăng tin.");
            return "redirect:/pele-dating";
        }

        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        Optional<Showtime> stOpt = showtimeRepository.findById(showtimeId);
        if (movieOpt.isEmpty() || stOpt.isEmpty()) {
            ra.addFlashAttribute("postError", "Phim hoặc suất chiếu không hợp lệ.");
            return "redirect:/pele-dating";
        }
        Showtime st = stOpt.get();
        if (st.getMovie() == null || !st.getMovie().getId().equals(movieId)) {
            ra.addFlashAttribute("postError", "Suất chiếu không khớp với phim đã chọn.");
            return "redirect:/pele-dating";
        }

        LocalDateTime watchWhen;
        try {
            watchWhen = parseDateTimeLocal(dateTime);
        } catch (DateTimeParseException ex) {
            ra.addFlashAttribute("postError", "Ngày giờ không hợp lệ.");
            return "redirect:/pele-dating";
        }

        if (peopleCount < 1 || peopleCount > 10) {
            ra.addFlashAttribute("postError", "Số người phải từ 1 đến 10.");
            return "redirect:/pele-dating";
        }

        String seat = seatPreference != null ? seatPreference.trim() : "";
        String th = theater != null ? theater.trim() : "";
        if (seat.isEmpty() || th.isEmpty()) {
            ra.addFlashAttribute("postError", "Vui lòng điền đủ thông tin.");
            return "redirect:/pele-dating";
        }

        Movie movie = movieOpt.get();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String showtimeLabel = st.getStartTime() != null
            ? st.getStartTime().format(dtf)
            : "N/A";
        if (st.getRoom() != null) {
            showtimeLabel = showtimeLabel + " · " + st.getRoom().getName();
        }
        String watchLabel = watchWhen.format(dtf);

        DatingMoviePost post = new DatingMoviePost(
            DATING_POST_ID.getAndIncrement(),
            user.getId(),
            profile.getDisplayName(),
            profile.getAvatarUrl(),
            movie.getTitle(),
            showtimeLabel,
            watchLabel,
            peopleCount,
            seat,
            th,
            LocalDateTime.now()
        );
        DATING_POSTS.add(0, post);
        ra.addFlashAttribute("postSuccess", "Đã đăng tin thành công!");
        return "redirect:/pele-dating";
    }

    private static LocalDateTime parseDateTimeLocal(String raw) {
        String s = raw != null ? raw.trim() : "";
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    // ============ LIKE / SEND REQUEST ============

    @PostMapping("/pele-dating/like/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendLike(HttpSession session, @PathVariable Long userId) {
        User user = getUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập."));
        Map<String, Object> result = datingService.sendRequest(user, userId);
        return ResponseEntity.ok(result);
    }

    // ============ RESPOND TO REQUEST ============

    @PostMapping("/pele-dating/respond/{requestId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> respondRequest(HttpSession session,
                                                             @PathVariable Long requestId,
                                                             @RequestParam boolean accept) {
        User user = getUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập."));
        Map<String, Object> result = datingService.respondToRequest(user, requestId, accept);
        return ResponseEntity.ok(result);
    }

    // ============ PROFILE POPUP ============

    @GetMapping("/pele-dating/profile/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProfilePopup(HttpSession session, @PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        Optional<DatingProfile> profileOpt = datingProfileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy hồ sơ."));
        }
        DatingProfile profile = profileOpt.get();
        result.put("profile", profile);
        result.put("userId", profile.getUser().getId());
        User me = getUser(session);
        if (me != null) {
            boolean alreadySent = datingService.hasPendingRequest(me.getId(), userId);
            boolean alreadyMatched = datingService.hasMatched(me.getId(), userId);
            result.put("alreadySent", alreadySent);
            result.put("alreadyMatched", alreadyMatched);
            if (alreadyMatched) {
                datingService.getMatchIdBetweenUsers(me.getId(), userId).ifPresent(mid -> result.put("matchId", mid));
            }
        }
        return ResponseEntity.ok(result);
    }

    // ============ SHOWTIMES FOR POST ============

    @GetMapping("/pele-dating/showtimes")
    @ResponseBody
    public List<Map<String, Object>> getShowtimes(@RequestParam Long movieId) {
        List<Map<String, Object>> result = new ArrayList<>();
        var showtimes = showtimeRepository.findByMovieId(movieId);
        for (var s : showtimes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("time", s.getStartTime() != null ? s.getStartTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A");
            map.put("room", s.getRoom() != null ? s.getRoom().getName() : "Phòng " + s.getId());
            map.put("price", s.getPrice());
            result.add(map);
        }
        return result;
    }

    // ============ CHAT ============

    @GetMapping("/pele-dating/chat/{matchId}")
    public String chat(HttpSession session, @PathVariable Long matchId, Model model) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";
        Optional<DatingMatch> matchOpt = datingService.getMatchById(matchId, user.getId());
        if (matchOpt.isEmpty()) return "redirect:/pele-dating";
        DatingMatch match = matchOpt.get();
        List<DatingMessage> messages = datingService.getMessages(matchId, user.getId());
        DatingProfile otherProfile = match.getOtherProfile(user);
        User otherUser = match.getOtherUser(user);
        model.addAttribute("match", match);
        model.addAttribute("messages", messages);
        model.addAttribute("otherProfile", otherProfile);
        model.addAttribute("otherUser", otherUser);
        model.addAttribute("currentUser", user);
        return "dating-chat";
    }

    @PostMapping("/pele-dating/chat/{matchId}/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(HttpSession session,
                                                         @PathVariable Long matchId,
                                                         @RequestParam String content) {
        User user = getUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập."));
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tin nhắn không được để trống."));
        }
        DatingMessage msg = datingService.sendMessage(matchId, user.getId(), content.trim());
        if (msg == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền gửi tin nhắn."));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", msg);
        return ResponseEntity.ok(result);
    }

    // ============ MATCHES LIST ============

    @GetMapping("/pele-dating/matches")
    @ResponseBody
    public List<Map<String, Object>> getMatches(HttpSession session) {
        User user = getUser(session);
        if (user == null) return Collections.emptyList();
        List<DatingMatch> matches = datingService.getUserMatches(user.getId());
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (DatingMatch m : matches) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            User otherUser = m.getOtherUser(user);
            DatingProfile otherProfile = m.getOtherProfile(user);
            map.put("userId", otherUser != null ? otherUser.getId() : null);
            map.put("displayName", otherProfile != null ? otherProfile.getDisplayName() : "Người dùng");
            map.put("avatarUrl", otherProfile != null ? otherProfile.getAvatarUrl() : null);
            map.put("createdAt", m.getCreatedAt().toString());
            List<DatingMessage> msgs = datingService.getMessages(m.getId(), user.getId());
            if (!msgs.isEmpty()) {
                DatingMessage last = msgs.get(msgs.size() - 1);
                map.put("lastMessage", last.getContent());
                map.put("lastMessageTime", last.getCreatedAt().toString());
                long unread = 0;
                for (DatingMessage mm : msgs) {
                    if (!mm.isRead() && !mm.getSender().getId().equals(user.getId())) unread++;
                }
                map.put("unreadCount", unread);
            } else {
                map.put("lastMessage", null);
                map.put("unreadCount", 0);
            }
            resultList.add(map);
        }
        return resultList;
    }

    // ============ CHAT MESSAGES ============

    @GetMapping("/pele-dating/matches/{matchId}/messages")
    @ResponseBody
    public List<Map<String, Object>> getMatchMessages(HttpSession session, @PathVariable Long matchId) {
        User user = getUser(session);
        if (user == null) return Collections.emptyList();
        List<DatingMessage> msgs = datingService.getMessages(matchId, user.getId());
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (DatingMessage m : msgs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSender().getId());
            map.put("content", m.getContent());
            map.put("isRead", m.isRead());
            map.put("createdAt", m.getCreatedAt().toString());
            map.put("isMine", m.getSender().getId().equals(user.getId()));
            resultList.add(map);
        }
        return resultList;
    }

    // ============ NOTIFICATIONS ============

    @GetMapping("/pele-dating/notifications")
    @ResponseBody
    public Map<String, Object> getNotifications(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = getUser(session);
        if (user == null) {
            result.put("count", 0);
            result.put("notifications", Collections.emptyList());
            return result;
        }
        List<DatingNotification> notifs = datingService.getUnshownNotifications(user.getId());
        List<Map<String, Object>> notifList = new ArrayList<>();
        for (DatingNotification n : notifs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("type", n.getType());
            map.put("fromUserId", n.getFromUser().getId());
            map.put("fromUserName", n.getFromUser().getFullName());
            DatingProfile dp = datingProfileRepository.findByUser(n.getFromUser()).orElse(null);
            if (dp != null) {
                map.put("displayName", dp.getDisplayName());
                map.put("avatarUrl", dp.getAvatarUrl());
            }
            if (n.getDatingMatch() != null) map.put("matchId", n.getDatingMatch().getId());
            if (n.getDatingRequest() != null) map.put("requestId", n.getDatingRequest().getId());
            map.put("createdAt", n.getCreatedAt().toString());
            notifList.add(map);
        }
        result.put("count", notifs.size());
        result.put("notifications", notifList);
        return result;
    }

    @PostMapping("/pele-dating/notifications/dismiss/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> dismissNotification(HttpSession session, @PathVariable Long id) {
        User user = getUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập."));
        datingService.dismissNotification(id, user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/pele-dating/notifications/dismiss-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> dismissAll(HttpSession session) {
        User user = getUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập."));
        datingService.dismissAllNotifications(user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** Display DTO for dating feed posts (in-memory). */
    public static class DatingMoviePost {
        private final long id;
        private final long userId;
        private final String authorName;
        private final String authorAvatar;
        private final String movieTitle;
        private final String showtimeLabel;
        private final String watchDateTimeLabel;
        private final int peopleCount;
        private final String seatPreference;
        private final String theater;
        private final LocalDateTime createdAt;

        public DatingMoviePost(long id, long userId, String authorName, String authorAvatar,
                               String movieTitle, String showtimeLabel, String watchDateTimeLabel,
                               int peopleCount, String seatPreference, String theater,
                               LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.authorName = authorName;
            this.authorAvatar = authorAvatar;
            this.movieTitle = movieTitle;
            this.showtimeLabel = showtimeLabel;
            this.watchDateTimeLabel = watchDateTimeLabel;
            this.peopleCount = peopleCount;
            this.seatPreference = seatPreference;
            this.theater = theater;
            this.createdAt = createdAt;
        }

        public long getId() { return id; }
        public long getUserId() { return userId; }
        public String getAuthorName() { return authorName; }
        public String getAuthorAvatar() { return authorAvatar; }
        public String getMovieTitle() { return movieTitle; }
        public String getShowtimeLabel() { return showtimeLabel; }
        public String getWatchDateTimeLabel() { return watchDateTimeLabel; }
        public int getPeopleCount() { return peopleCount; }
        public String getSeatPreference() { return seatPreference; }
        public String getTheater() { return theater; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    // Helper class for dating post
    public static class DatingPostData {
        public Long movieId;
        public Long showtimeId;
        public String dateTime;
        public String seatPreference;
        public int peopleCount = 1;
        public String theater;
        public Long getMovieId() { return movieId; }
        public void setMovieId(Long movieId) { this.movieId = movieId; }
        public Long getShowtimeId() { return showtimeId; }
        public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }
        public String getSeatPreference() { return seatPreference; }
        public void setSeatPreference(String seatPreference) { this.seatPreference = seatPreference; }
        public int getPeopleCount() { return peopleCount; }
        public void setPeopleCount(int peopleCount) { this.peopleCount = peopleCount; }
        public String getTheater() { return theater; }
        public void setTheater(String theater) { this.theater = theater; }
    }
}
