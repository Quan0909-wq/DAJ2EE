package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.*;
import com.example.demoj2ee.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private GroupBookingRepository groupBookingRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private TicketDisputeRepository ticketDisputeRepository;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }

    // ================== TRANG CHÍNH ADMIN ==================

    @GetMapping
    public String adminDashboard(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        List<Booking> allBookings = bookingRepository.findAll();
        List<Movie> movies = movieRepository.findAll();

        List<Booking> recentBookings = allBookings.stream()
                .sorted((a, b) -> b.getBookingTime().compareTo(a.getBookingTime()))
                .toList();

        double todayRevenue = allBookings.stream()
                .filter(b -> b.getBookingTime() != null && b.getBookingTime().toLocalDate().equals(LocalDate.now()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        double totalRevenue = allBookings.stream()
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        long successBookings = allBookings.stream()
                .filter(b -> "SUCCESS".equalsIgnoreCase(b.getStatus()))
                .count();

        long pendingBookings = allBookings.stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                .count();

        long cancelledBookings = allBookings.stream()
                .filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus()))
                .count();

        String topMovieTitle = "Chưa có dữ liệu";
        Map<String, Long> movieStats = allBookings.stream()
                .filter(b -> b.getShowtime() != null && b.getShowtime().getMovie() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        b -> b.getShowtime().getMovie().getTitle(),
                        java.util.stream.Collectors.counting()
                ));

        if (!movieStats.isEmpty()) {
            topMovieTitle = movieStats.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Chưa có dữ liệu");
        }

        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("totalMovies", movies.size());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("successBookings", successBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("topMovieTitle", topMovieTitle);
        model.addAttribute("recentBookings", recentBookings.stream().limit(10).toList());

        return "admin/dashboard";
    }

    // ================== QUẢN LÝ PHIM ==================

    @GetMapping("/movies")
    public String manageMovies(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("movies", movieRepository.findAll());
        return "admin/quan-ly-phim";
    }

    @GetMapping("/movies/add")
    public String addMovieForm(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("movie", new Movie());
        return "admin/movie-form";
    }

    @PostMapping("/movies/save")
    public String saveMovie(@RequestParam(required = false) Long id,
                            @RequestParam String title,
                            @RequestParam String description,
                            @RequestParam String director,
                            @RequestParam String cast,
                            @RequestParam int duration,
                            @RequestParam String releaseDate,
                            @RequestParam String posterUrl,
                            @RequestParam String genre,
                            @RequestParam String trailerUrl,
                            RedirectAttributes ra) {
        Movie movie = (id != null) ? movieRepository.findById(id).orElse(new Movie()) : new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDirector(director);
        movie.setCast(cast);
        movie.setDuration(duration);
        movie.setReleaseDate(LocalDate.parse(releaseDate));
        movie.setPosterUrl(posterUrl);
        movie.setGenre(genre);
        movie.setTrailerUrl(trailerUrl);
        movieRepository.save(movie);
        ra.addFlashAttribute("success", "Lưu phim thành công!");
        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/edit/{id}")
    public String editMovieForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) return "redirect:/admin/movies";
        model.addAttribute("movie", movie);
        return "admin/movie-form";
    }

    @GetMapping("/movies/delete/{id}")
    public String deleteMovie(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        movieRepository.deleteById(id);
        ra.addFlashAttribute("success", "Xóa phim thành công!");
        return "redirect:/admin/movies";
    }

    // ================== QUẢN LÝ HOÁ ĐƠN ==================

    @GetMapping("/bookings")
    public String manageBookings(HttpSession session,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String date,
                                 Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        List<Booking> bookings = bookingRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {
            bookings = bookings.stream()
                    .filter(b -> (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(search.toLowerCase())) ||
                            (b.getCustomerEmail() != null && b.getCustomerEmail().toLowerCase().contains(search.toLowerCase())))
                    .toList();
        }
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            bookings = bookings.stream().filter(b -> status.equals(b.getStatus())).toList();
        }
        if (date != null && !date.isEmpty()) {
            LocalDate searchDate = LocalDate.parse(date);
            bookings = bookings.stream().filter(b -> b.getBookingTime().toLocalDate().equals(searchDate)).toList();
        }

        double totalRevenue = bookingRepository.findAll().stream().mapToDouble(Booking::getTotalAmount).sum();
        double filteredRevenue = bookings.stream().mapToDouble(Booking::getTotalAmount).sum();
        bookings = bookings.stream().sorted((a, b) -> b.getBookingTime().compareTo(a.getBookingTime())).toList();

        model.addAttribute("bookings", bookings);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("filteredRevenue", filteredRevenue);
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("selectedStatus", status != null ? status : "all");
        model.addAttribute("selectedDate", date != null ? date : "");
        return "admin/quan-ly-hoa-don";
    }

    @PostMapping("/bookings/update-status")
    public String updateBookingStatus(@RequestParam Long id, @RequestParam String status,
                                      HttpSession session, RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setStatus(status);
            bookingRepository.save(booking);
            ra.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/bookings/detail/{id}")
    public String bookingDetail(@PathVariable Long id, HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return "redirect:/admin/bookings";
        model.addAttribute("booking", booking);
        return "admin/booking-detail";
    }

    // ================== QUẢN LÝ SUẤT CHIẾU ==================

    @GetMapping("/showtimes")
    public String manageShowtimes(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        List<Room> rooms = roomRepository.findAll();
        if (rooms == null) rooms = new ArrayList<>();
        model.addAttribute("showtimes", showtimeRepository.findAll());
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("rooms", rooms);
        return "admin/quan-ly-suat-chieu";
    }

    @PostMapping("/showtimes/save")
    public String saveShowtime(@RequestParam(required = false) Long id,
                               @RequestParam Long movieId,
                               @RequestParam Long roomId,
                               @RequestParam String startTime,
                               @RequestParam double price,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        Showtime showtime = (id != null) ? showtimeRepository.findById(id).orElse(new Showtime()) : new Showtime();
        Movie movie = movieRepository.findById(movieId).orElse(null);
        Room room = roomRepository.findById(roomId).orElse(null);

        if (movie != null && room != null) {
            showtime.setMovie(movie);
            showtime.setRoom(room);
            showtime.setStartTime(LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            showtime.setPrice(price);
            showtimeRepository.save(showtime);
            ra.addFlashAttribute("success", "Lưu suất chiếu thành công!");
        } else {
            if (room == null) ra.addFlashAttribute("error", "Vui lòng chọn phòng chiếu hợp lệ.");
            else if (movie == null) ra.addFlashAttribute("error", "Vui lòng chọn phim hợp lệ.");
        }
        return "redirect:/admin/showtimes";
    }

    @GetMapping("/showtimes/delete/{id}")
    public String deleteShowtime(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        showtimeRepository.deleteById(id);
        ra.addFlashAttribute("success", "Xóa suất chiếu thành công!");
        return "redirect:/admin/showtimes";
    }

    // ================== QUẢN LÝ ĐẶT VÉ NHÓM ==================

    @GetMapping("/group-bookings")
    public String manageGroupBookings(HttpSession session,
                                      @RequestParam(required = false) String status,
                                      Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        List<GroupBooking> all = groupBookingRepository.findAll();

        List<GroupBooking> bookings;
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            if ("SUCCESS".equals(status)) {
                bookings = all.stream().filter(b -> b.getStatus() == 2).toList();
            } else if ("PENDING".equals(status)) {
                bookings = all.stream().filter(b -> b.getStatus() == 0 || b.getStatus() == 1).toList();
            } else if ("CANCELLED".equals(status)) {
                bookings = all.stream().filter(b -> b.getStatus() == 3).toList();
            } else {
                bookings = all;
            }
        } else {
            bookings = all;
        }

        bookings = bookings.stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime tb = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                    return tb.compareTo(ta);
                })
                .toList();

        long totalBookings = groupBookingRepository.count();
        long successBookings = all.stream().filter(b -> b.getStatus() == 2).count();
        long pendingBookings = all.stream().filter(b -> b.getStatus() == 0 || b.getStatus() == 1).count();
        long cancelledBookings = all.stream().filter(b -> b.getStatus() == 3).count();

        Map<Long, Long> memberCounts = new HashMap<>();
        for (GroupBooking b : bookings) {
            memberCounts.put(b.getId(), groupMemberRepository.countByGroupBookingId(b.getId()));
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("memberCounts", memberCounts);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("successBookings", successBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("selectedStatus", status != null ? status : "all");
        return "admin/quan-ly-dat-ve-nhom";
    }

    @GetMapping("/group-booking/{id}")
    public String groupBookingDetail(@PathVariable Long id, HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        GroupBooking booking = groupBookingRepository.findById(id).orElse(null);
        if (booking == null) return "redirect:/admin/group-bookings";
        List<GroupMember> members = groupMemberRepository.findByGroupBookingIdOrderById(id);
        model.addAttribute("booking", booking);
        model.addAttribute("members", members);
        return "admin/group-booking-detail";
    }

    @PostMapping("/group-bookings/update-status")
    public String updateGroupBookingStatus(@RequestParam Long id, @RequestParam String status,
                                           HttpSession session, RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        GroupBooking booking = groupBookingRepository.findById(id).orElse(null);
        if (booking != null) {
            int statusInt = switch (status) {
                case "SUCCESS" -> 2;
                case "PENDING" -> 0;
                case "CANCELLED" -> 3;
                default -> booking.getStatus();
            };
            booking.setStatus(statusInt);
            groupBookingRepository.save(booking);
            ra.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/admin/group-bookings";
    }

    // ================== QUẢN LÝ TIN TỨC (NEWS) ==================

    @GetMapping("/news")
    public String manageNews(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("newsList", newsRepository.findAll().stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getPublishedAt() != null ? a.getPublishedAt() : LocalDateTime.MIN;
                    LocalDateTime tb = b.getPublishedAt() != null ? b.getPublishedAt() : LocalDateTime.MIN;
                    return tb.compareTo(ta);
                }).toList());
        return "admin/quan-ly-tin-tuc";
    }

    @GetMapping("/news/add")
    public String addNewsForm(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("newsItem", new News());
        return "admin/news-form";
    }

    @PostMapping("/news/save")
    public String saveNews(@RequestParam(required = false) Long id,
                           @RequestParam String title,
                           @RequestParam String content,
                           @RequestParam String excerpt,
                           @RequestParam String imageUrl,
                           @RequestParam String category,
                           @RequestParam String author,
                           HttpSession session,
                           RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        News news = (id != null) ? newsRepository.findById(id).orElse(new News()) : new News();
        news.setTitle(title);
        news.setContent(content);
        news.setExcerpt(excerpt);
        news.setImageUrl(imageUrl);
        news.setCategory(category);
        news.setAuthor(author);
        news.setActive(true);
        if (news.getPublishedAt() == null) news.setPublishedAt(LocalDateTime.now());
        newsRepository.save(news);
        ra.addFlashAttribute("success", "Lưu tin tức thành công!");
        return "redirect:/admin/news";
    }

    @GetMapping("/news/edit/{id}")
    public String editNewsForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        News news = newsRepository.findById(id).orElse(null);
        if (news == null) return "redirect:/admin/news";
        model.addAttribute("newsItem", news);
        return "admin/news-form";
    }

    @GetMapping("/news/delete/{id}")
    public String deleteNews(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        newsRepository.deleteById(id);
        ra.addFlashAttribute("success", "Xóa tin tức thành công!");
        return "redirect:/admin/news";
    }

    // ================== QUẢN LÝ KHUYẾN MÃI (PROMOTIONS) ==================

    @GetMapping("/promotions")
    public String managePromotions(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        List<Promotion> promos = promotionRepository.findAll().stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime tb = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                    return tb.compareTo(ta);
                }).toList();
        model.addAttribute("promotions", promos);
        model.addAttribute("showtimes", showtimeRepository.findAll());
        return "admin/quan-ly-khuyen-mai";
    }

    @GetMapping("/promotions/add")
    public String addPromotionForm(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("showtimes", showtimeRepository.findAll());
        return "admin/promotion-form";
    }

    @PostMapping("/promotions/save")
    public String savePromotion(@RequestParam(required = false) Long id,
                                @RequestParam String code,
                                @RequestParam String title,
                                @RequestParam String description,
                                @RequestParam(defaultValue = "0") double discountAmount,
                                @RequestParam(defaultValue = "0") int discountPercent,
                                @RequestParam String expiresAt,
                                @RequestParam(required = false) Long showtimeId,
                                HttpSession session,
                                RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        Promotion promo = (id != null) ? promotionRepository.findById(id).orElse(new Promotion()) : new Promotion();

        // Kiểm tra trùng mã (cho phép cập nhật nếu cùng 1 record)
        if (promo.getId() == null) {
            Optional<Promotion> existing = promotionRepository.findByCode(code.trim().toUpperCase());
            if (existing.isPresent()) {
                ra.addFlashAttribute("error", "Mã khuyến mãi '" + code + "' đã tồn tại!");
                return "redirect:/admin/promotions";
            }
        }

        promo.setCode(code.trim().toUpperCase());
        promo.setTitle(title);
        promo.setDescription(description);
        promo.setDiscountAmount(discountAmount);
        promo.setDiscountPercent(discountPercent);
        promo.setActive(true);
        if (expiresAt != null && !expiresAt.isEmpty()) {
            promo.setExpiresAt(LocalDateTime.parse(expiresAt + "T23:59:59"));
        } else {
            promo.setExpiresAt(null);
        }
        if (showtimeId != null) {
            promo.setShowtime(showtimeRepository.findById(showtimeId).orElse(null));
        } else {
            promo.setShowtime(null);
        }
        promotionRepository.save(promo);
        ra.addFlashAttribute("success", "Lưu khuyến mãi thành công!");
        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotions/edit/{id}")
    public String editPromotionForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        Promotion promo = promotionRepository.findById(id).orElse(null);
        if (promo == null) return "redirect:/admin/promotions";
        model.addAttribute("promotion", promo);
        model.addAttribute("showtimes", showtimeRepository.findAll());
        return "admin/promotion-form";
    }

    @GetMapping("/promotions/delete/{id}")
    public String deletePromotion(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";
        promotionRepository.deleteById(id);
        ra.addFlashAttribute("success", "Xóa khuyến mãi thành công!");
        return "redirect:/admin/promotions";
    }
    // ================== QUẢN LÝ NGƯỜI DÙNG ==================

    @GetMapping("/users")
    public String manageUsers(HttpSession session,
                              @RequestParam(required = false) String keyword,
                              Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        List<User> users;

        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository
                    .findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            keyword.trim(), keyword.trim(), keyword.trim()
                    );
        } else {
            users = userRepository.findAll();
        }

        users = users.stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "admin/admin-users";
    }

    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        User currentUser = (User) session.getAttribute("loggedInUser");
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }

        // Không cho admin tự khóa chính mình
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Bạn không thể tự khóa tài khoản của chính mình!");
            return "redirect:/admin/users";
        }

        user.setActive(!user.isActive());
        userRepository.save(user);

        ra.addFlashAttribute("success",
                user.isActive() ? "Đã mở khóa tài khoản thành công!" : "Đã khóa tài khoản thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/toggle-role/{id}")
    public String toggleUserRole(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        User currentUser = (User) session.getAttribute("loggedInUser");
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }

        // Không cho admin tự đổi role của chính mình
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Bạn không thể tự đổi quyền của chính mình!");
            return "redirect:/admin/users";
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            user.setRole("USER");
        } else {
            user.setRole("ADMIN");
        }

        userRepository.save(user);
        ra.addFlashAttribute("success", "Cập nhật quyền người dùng thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        User currentUser = (User) session.getAttribute("loggedInUser");
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }

        // Không cho admin tự xóa chính mình
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Bạn không thể tự xóa tài khoản của chính mình!");
            return "redirect:/admin/users";
        }

        userRepository.delete(user);
        ra.addFlashAttribute("success", "Xóa người dùng thành công!");
        return "redirect:/admin/users";
    }
    // ================== QUẢN LÝ KHIẾU NẠI VÉ ==================

    @GetMapping("/disputes")
    public String manageDisputes(HttpSession session,
                                 @RequestParam(required = false) String status,
                                 Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        List<TicketDispute> disputes;

        if (status != null && !status.isEmpty() && !status.equals("all")) {
            disputes = ticketDisputeRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            disputes = ticketDisputeRepository.findAllByOrderByCreatedAtDesc();
        }

        model.addAttribute("disputes", disputes);
        model.addAttribute("selectedStatus", status != null ? status : "all");
        return "admin/quan-ly-khieu-nai";
    }

    @GetMapping("/disputes/{id}")
    public String disputeDetail(@PathVariable Long id,
                                HttpSession session,
                                Model model,
                                RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        TicketDispute dispute = ticketDisputeRepository.findById(id).orElse(null);
        if (dispute == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khiếu nại!");
            return "redirect:/admin/disputes";
        }

        model.addAttribute("dispute", dispute);
        return "admin/khieu-nai-detail";
    }

    @PostMapping("/disputes/update")
    public String updateDispute(@RequestParam Long id,
                                @RequestParam String status,
                                @RequestParam(required = false) String resolutionType,
                                @RequestParam(required = false) String adminReply,
                                HttpSession session,
                                RedirectAttributes ra) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        TicketDispute dispute = ticketDisputeRepository.findById(id).orElse(null);
        if (dispute == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khiếu nại!");
            return "redirect:/admin/disputes";
        }

        dispute.setStatus(status);
        dispute.setAdminReply(adminReply);
        dispute.setResolutionType(resolutionType);

        if ("RESOLVED".equals(status) || "REJECTED".equals(status)) {
            dispute.setResolvedAt(LocalDateTime.now());
        } else {
            dispute.setResolvedAt(null);
        }

        Booking booking = dispute.getBooking();
        if (booking != null) {
            booking.setAllowSeatChange(false);
            booking.setAllowShowtimeChange(false);
            booking.setAllowMovieChange(false);
            booking.setAllowCancelBooking(false);
            booking.setDisputeNote(adminReply);

            if ("RESOLVED".equals(status)) {
                dispute.setCustomerActionRequired(true);
                dispute.setCustomerActionDone(false);

                if ("CHANGE_SEAT".equals(resolutionType)) {
                    booking.setAllowSeatChange(true);
                } else if ("CHANGE_SHOWTIME".equals(resolutionType)) {
                    booking.setAllowShowtimeChange(true);
                } else if ("CHANGE_MOVIE".equals(resolutionType)) {
                    booking.setAllowMovieChange(true);
                } else if ("CANCEL_BOOKING".equals(resolutionType)) {
                    booking.setAllowCancelBooking(true);
                } else {
                    dispute.setCustomerActionRequired(false);
                }
            } else {
                dispute.setCustomerActionRequired(false);
                dispute.setCustomerActionDone(false);
            }

            bookingRepository.save(booking);
        }

        ticketDisputeRepository.save(dispute);
        ra.addFlashAttribute("success", "Cập nhật khiếu nại thành công!");
        return "redirect:/admin/disputes/" + id;
    }
}