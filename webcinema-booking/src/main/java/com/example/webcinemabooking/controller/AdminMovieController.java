package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Movie;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.MovieRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/movies")
public class AdminMovieController {

    @Autowired
    private MovieRepository movieRepository;

    // Hàm kiểm tra quyền Admin
    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // 1. HIỂN THỊ DANH SÁCH PHIM
    @GetMapping
    public String listMovies(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        List<Movie> movies = movieRepository.findAll();
        model.addAttribute("movies", movies);
        return "admin/movies";
    }

    // 2. HIỂN THỊ FORM THÊM PHIM MỚI
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        model.addAttribute("movie", new Movie()); // Gửi một object Movie rỗng ra form
        return "admin/movie-form";
    }

    // 3. HIỂN THỊ FORM SỬA PHIM
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) return "redirect:/admin/movies";

        model.addAttribute("movie", movie);
        return "admin/movie-form";
    }

    // 4. LƯU PHIM VÀO DATABASE (Dùng chung cho cả Thêm và Sửa)
    @PostMapping("/save")
    public String saveMovie(@ModelAttribute Movie movie, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";

        movieRepository.save(movie); // Lưu vào DB
        return "redirect:/admin/movies"; // Lưu xong quay về trang danh sách
    }

    // 5. XÓA PHIM
    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable("id") Long id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";

        movieRepository.deleteById(id);
        return "redirect:/admin/movies";
    }
}