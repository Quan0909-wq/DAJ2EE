package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Showtime;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.MovieRepository;
import com.example.demoj2ee.repository.RoomRepository;
import com.example.demoj2ee.repository.ShowtimeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/showtimes")
public class AdminShowtimeController {

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private RoomRepository roomRepository;

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping
    public String listShowtimes(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        List<Showtime> showtimes = showtimeRepository.findAll();
        model.addAttribute("showtimes", showtimes);
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/quan-ly-suat-chieu";
    }

    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";
        model.addAttribute("showtime", new Showtime());
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/quan-ly-suat-chieu";
    }

    @PostMapping("/save")
    public String saveShowtime(
            @RequestParam("movieId") Long movieId,
            @RequestParam("roomId") Long roomId,
            @RequestParam("startTime") String startTime,
            @RequestParam("price") double price,
            HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";
        Showtime showtime = new Showtime();
        showtime.setMovie(movieRepository.findById(movieId).orElse(null));
        showtime.setRoom(roomRepository.findById(roomId).orElse(null));
        showtime.setStartTime(LocalDateTime.parse(startTime));
        showtime.setPrice(price);
        showtimeRepository.save(showtime);
        return "redirect:/admin/showtimes";
    }

    @GetMapping("/delete/{id}")
    public String deleteShowtime(@PathVariable("id") Long id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";
        showtimeRepository.deleteById(id);
        return "redirect:/admin/showtimes";
    }
}
