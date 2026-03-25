package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Comment;
import com.example.webcinemabooking.model.Movie;
import com.example.webcinemabooking.model.Showtime;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.CommentRepository;
import com.example.webcinemabooking.repository.MovieRepository;
import com.example.webcinemabooking.repository.ShowtimeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private CommentRepository commentRepository;

    // --- SỬA LẠI: Hàm trang chủ giờ biết nhận thêm Thể loại (Genre) ---
    // ...
    @GetMapping("/")
    public String showHomePage(@RequestParam(value = "genre", required = false) String genre, Model model) {

        List<Movie> movies;

        if (genre != null && !genre.isEmpty()) {
            // ĐỔI TÊN HÀM Ở DÒNG NÀY THÀNH CHỮ CHỨA CONTAINING NHÉ SẾP
            movies = movieRepository.findByGenreContainingIgnoreCase(genre);
            model.addAttribute("selectedGenre", genre);
        } else {
            movies = movieRepository.findAll();
            model.addAttribute("selectedGenre", null);
        }

        model.addAttribute("movies", movies);
        return "home";
    }
    // ...

    // --- MỚI THÊM: Xử lý khi khách gõ vào ô tìm kiếm ---
    @GetMapping("/search")
    public String searchMovies(@RequestParam("keyword") String keyword, Model model) {

        // Tìm phim có chứa từ khóa đó trong tên
        List<Movie> searchResults = movieRepository.findByTitleContainingIgnoreCase(keyword);

        model.addAttribute("movies", searchResults);
        // Để nó không tô đỏ nút thể loại nào cả khi đang tìm kiếm
        model.addAttribute("selectedGenre", "searching...");

        return "home"; // Đẩy kết quả về lại trang chủ cho nó ngầu
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN 100% ---

    @GetMapping("/movie/{id}")
    public String movieDetail(@PathVariable("id") Long id, Model model) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) {
            return "redirect:/";
        }

        List<Showtime> showtimes = showtimeRepository.findByMovieId(id);
        List<Comment> comments = commentRepository.findByMovieIdOrderByCreatedAtDesc(id);

        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);
        model.addAttribute("comments", comments);

        return "movie-detail";
    }

    @PostMapping("/movie/comment")
    public String postComment(@RequestParam("movieId") Long movieId,
                              @RequestParam("content") String content,
                              @RequestParam("rating") int rating,
                              HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Comment comment = new Comment();
        comment.setMovie(movieRepository.findById(movieId).orElse(null));
        comment.setUser(user);
        comment.setContent(content);
        comment.setRating(rating);
        commentRepository.save(comment);

        System.out.println("💬 " + user.getFullName() + " vừa chấm " + rating + " sao cho phim ID: " + movieId);

        return "redirect:/movie/" + movieId;
    }

    @GetMapping("/upcoming")
    public String showUpcoming(Model model) {
        return "upcoming";
    }

    @GetMapping("/news")
    public String showNews() {
        return "news";
    }

    @GetMapping("/news/{id}")
    public String newsDetail(@PathVariable Long id, Model model) {
        return "news-detail";
    }

    @GetMapping("/news/detail/{id}")
    public String showNewsDetail(@PathVariable Long id, Model model) {
        model.addAttribute("author", "Admin Quan");
        if (id == 1) {
            return "marvel-review";
        } else {
            return "promo-bap-nuoc";
        }
    }

    @GetMapping("/promotions")
    public String showPromotions() {
        return "promotions";
    }
}