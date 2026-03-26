package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Comment;
import com.example.demoj2ee.model.Movie;
import com.example.demoj2ee.model.Showtime;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.MovieRepository;
import com.example.demoj2ee.repository.ShowtimeRepository;
import com.example.demoj2ee.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class HomeController {

    /** Chuyen YouTube watch URL sang embed URL de iframe hien thi duoc */
    private String toYoutubeEmbedUrl(String url) {
        if (url == null || url.trim().isEmpty()) return "";
        String u = url.trim();
        // Da la embed URL
        if (u.contains("youtube.com/embed/")) return u;
        // youtube.com/watch?v=VIDEO_ID
        Pattern p1 = Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");
        Matcher m1 = p1.matcher(u);
        if (m1.find()) return "https://www.youtube.com/embed/" + m1.group(1);
        return u;
    }

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private CommentService commentService;

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
        List<Comment> comments = commentService.getCommentsByMovie(id);

        model.addAttribute("movie", movie);
        model.addAttribute("trailerEmbedUrl", toYoutubeEmbedUrl(movie.getTrailerUrl()));
        model.addAttribute("showtimes", showtimes);
        model.addAttribute("comments", comments);

        return "movie-detail";
    }

    @PostMapping("/movie/comment")
    public String postComment(@RequestParam Long movieId,
                             @RequestParam String content,
                             @RequestParam int rating,
                             jakarta.servlet.http.HttpSession session,
                             RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        commentService.saveComment(movieId, user.getUsername(), content, rating);
        ra.addFlashAttribute("success", "Binh luan cua ban da duoc dang!");
        return "redirect:/movie/" + movieId;
    }

    // Các trang tĩnh
    @GetMapping("/upcoming")
    public String showUpcoming() { return "upcoming"; }

    @GetMapping("/news")
    public String showNews() { return "news"; }

    @GetMapping("/promotions")
    public String showPromotions() { return "promotions"; }
}