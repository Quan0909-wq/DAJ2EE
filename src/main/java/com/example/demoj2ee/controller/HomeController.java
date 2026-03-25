package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Comment;
import com.example.demoj2ee.model.Movie;
import com.example.demoj2ee.model.Showtime;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.CommentRepository;
import com.example.demoj2ee.repository.MovieRepository;
import com.example.demoj2ee.repository.ShowtimeRepository;
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

    @GetMapping("/")
    public String showHomePage(@RequestParam(value = "genre", required = false) String genre, Model model) {
        List<Movie> movies;

        if (genre != null && !genre.isEmpty()) {
            movies = movieRepository.findByGenreContainingIgnoreCase(genre);
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
        List<Movie> searchResults = movieRepository.findByTitleContainingIgnoreCase(keyword);
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
