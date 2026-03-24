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
import java.util.List;

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

        List<Booking> recentBookings = bookingRepository.findAll().stream()
                .sorted((a, b) -> b.getBookingTime().compareTo(a.getBookingTime()))
                .toList();

        List<Movie> movies = movieRepository.findAll();

        double todayRevenue = recentBookings.stream()
                .filter(b -> b.getBookingTime().toLocalDate().equals(LocalDate.now()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("totalMovies", movies.size());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("recentBookings", recentBookings.stream().limit(10).toList());

        return "admin/dashboard";
    }

    // ================== QUẢN LÝ PHIM ==================

    @GetMapping("/movies")
    public String manageMovies(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/";

        List<Movie> movies = movieRepository.findAll();
        model.addAttribute("movies", movies);
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
        Movie movie;
        if (id != null) {
            movie = movieRepository.findById(id).orElse(new Movie());
        } else {
            movie = new Movie();
        }

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
            bookings = bookings.stream()
                    .filter(b -> status.equals(b.getStatus()))
                    .toList();
        }

        if (date != null && !date.isEmpty()) {
            LocalDate searchDate = LocalDate.parse(date);
            bookings = bookings.stream()
                    .filter(b -> b.getBookingTime().toLocalDate().equals(searchDate))
                    .toList();
        }

        final double totalRevenue = bookingRepository.findAll().stream()
                .mapToDouble(Booking::getTotalAmount).sum();

        final double filteredRevenue = bookings.stream()
                .mapToDouble(Booking::getTotalAmount).sum();

        bookings = bookings.stream()
                .sorted((a, b) -> b.getBookingTime().compareTo(a.getBookingTime()))
                .toList();

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
    public String updateBookingStatus(@RequestParam Long id,
                                      @RequestParam String status,
                                      HttpSession session,
                                      RedirectAttributes ra) {
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

        List<Showtime> showtimes = showtimeRepository.findAll();
        List<Movie> movies = movieRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        if (rooms == null) rooms = new java.util.ArrayList<>();

        model.addAttribute("showtimes", showtimes);
        model.addAttribute("movies", movies);
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

        Showtime showtime;
        if (id != null) {
            showtime = showtimeRepository.findById(id).orElse(new Showtime());
        } else {
            showtime = new Showtime();
        }

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
}
