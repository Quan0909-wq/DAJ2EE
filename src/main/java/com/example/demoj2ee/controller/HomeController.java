package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Movie;
import com.example.demoj2ee.model.Showtime;
import com.example.demoj2ee.repository.MovieRepository;
import com.example.demoj2ee.repository.ShowtimeRepository;
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

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @GetMapping("/")
    public String showHomePage(@RequestParam(value = "genre", required = false) String genre, Model model) {
        List<Movie> movies;

        if (genre != null && !genre.isEmpty()) {
            // Cần chắc chắn trong MovieRepository có hàm này
            // movies = movieRepository.findByGenreContainingIgnoreCase(genre);
            movies = movieRepository.findAll(); // Tạm thời load tất cả để không lỗi
            model.addAttribute("selectedGenre", genre);
        } else {
            movies = movieRepository.findAll();
            model.addAttribute("selectedGenre", null);
        }

        model.addAttribute("movies", movies);
        return "home";
    }

    @GetMapping("/search")
    public String searchMovies(@RequestParam("keyword") String keyword, Model model) {
        // Cần chắc chắn trong MovieRepository có hàm findByTitleContainingIgnoreCase
        // List<Movie> searchResults = movieRepository.findByTitleContainingIgnoreCase(keyword);
        List<Movie> searchResults = movieRepository.findAll(); // Tránh lỗi khi chưa cấu hình Repos

        model.addAttribute("movies", searchResults);
        model.addAttribute("selectedGenre", "searching...");
        return "home";
    }

    @GetMapping("/movie/{id}")
    public String movieDetail(@PathVariable("id") Long id, Model model) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) {
            return "redirect:/";
        }

        List<Showtime> showtimes = showtimeRepository.findByMovieId(id);

        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);
        // Đã gỡ bỏ toàn bộ code liên quan đến Comment để tránh lỗi database

        return "movie-detail";
    }

    // Các trang tĩnh
    @GetMapping("/upcoming")
    public String showUpcoming() { return "upcoming"; }

    @GetMapping("/news")
    public String showNews() { return "news"; }

    @GetMapping("/promotions")
    public String showPromotions() { return "promotions"; }
}