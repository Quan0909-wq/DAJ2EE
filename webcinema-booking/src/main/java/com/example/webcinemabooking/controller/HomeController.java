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

    // --- MỚI THÊM: Quản lý bình luận ---
    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("movies", movieRepository.findAll());
        return "home";
    }

    @GetMapping("/movie/{id}")
    public String movieDetail(@PathVariable("id") Long id, Model model) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) {
            return "redirect:/";
        }

        List<Showtime> showtimes = showtimeRepository.findByMovieId(id);

        // --- MỚI THÊM: Lấy danh sách bình luận của phim này ra ---
        List<Comment> comments = commentRepository.findByMovieIdOrderByCreatedAtDesc(id);

        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);
        model.addAttribute("comments", comments); // Gửi mớ bình luận sang HTML

        return "movie-detail";
    }

    // --- MỚI THÊM: Hàm bắt bưu kiện bình luận từ khách gửi lên ---
    @PostMapping("/movie/comment")
    public String postComment(@RequestParam("movieId") Long movieId,
                              @RequestParam("content") String content,
                              @RequestParam("rating") int rating,
                              HttpSession session) {

        // Kiểm tra xem khách đã đăng nhập chưa
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login"; // Chưa đăng nhập thì đuổi đi đăng nhập
        }

        // Tạo một bình luận mới
        Comment comment = new Comment();
        comment.setMovie(movieRepository.findById(movieId).orElse(null));
        comment.setUser(user);
        comment.setContent(content);
        comment.setRating(rating);

        // Lưu xuống Database
        commentRepository.save(comment);

        System.out.println("💬 " + user.getFullName() + " vừa chấm " + rating + " sao cho phim ID: " + movieId);

        return "redirect:/movie/" + movieId; // Đăng xong thì load lại trang phim để xem thành quả
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