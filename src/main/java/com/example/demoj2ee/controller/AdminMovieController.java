package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Movie;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.MovieRepository;
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

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping
    public String listMovies(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        List<Movie> movies = movieRepository.findAll();
        model.addAttribute("movies", movies);
        return "admin/quan-ly-phim";
    }

    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        model.addAttribute("movie", new Movie());
        return "admin/movie-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) return "redirect:/admin/movies";
        model.addAttribute("movie", movie);
        return "admin/movie-form";
    }

    @PostMapping("/save")
    public String saveMovie(@ModelAttribute Movie movie, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";
        movieRepository.save(movie);
        return "redirect:/admin/movies";
    }

    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable("id") Long id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";
        movieRepository.deleteById(id);
        return "redirect:/admin/movies";
    }
}
