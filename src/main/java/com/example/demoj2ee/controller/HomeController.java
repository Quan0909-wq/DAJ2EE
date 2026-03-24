package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Movie;
import com.example.demoj2ee.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private MovieRepository movieRepository;

    // 1. TRANG CHỦ: Hiển thị danh sách phim & Lọc theo thể loại
    @GetMapping("/")
    public String showHomePage(@RequestParam(value = "genre", required = false) String genre, Model model) {
        List<Movie> movies;

        if (genre != null && !genre.isEmpty()) {
            // Tìm phim theo thể loại (Nhớ thêm hàm này vào MovieRepository nha sếp)
            movies = movieRepository.findByGenreContainingIgnoreCase(genre);
            model.addAttribute("selectedGenre", genre);
        } else {
            // Hiện tất cả phim
            movies = movieRepository.findAll();
            model.addAttribute("selectedGenre", null);
        }

        model.addAttribute("movies", movies);
        return "home";
    }

    // 2. TÌM KIẾM PHIM: Theo từ khóa
    @GetMapping("/search")
    public String searchMovies(@RequestParam("keyword") String keyword, Model model) {
        List<Movie> searchResults = movieRepository.findByTitleContainingIgnoreCase(keyword);
        model.addAttribute("movies", searchResults);
        model.addAttribute("selectedGenre", "searching...");
        return "home";
    }

    // 3. CHI TIẾT PHIM: Nơi có nút dẫn tới /book-seat
    @GetMapping("/movie/{id}")
    public String movieDetail(@PathVariable("id") Long id, Model model) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) {
            return "redirect:/";
        }

        // Chỉ gửi thông tin phim sang trang chi tiết
        model.addAttribute("movie", movie);
        return "movie-detail";
    }
}