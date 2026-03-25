package com.example.webcinemabooking.controller;

import com.example.webcinemabooking.model.Showtime;
import com.example.webcinemabooking.model.User;
import com.example.webcinemabooking.repository.MovieRepository;
import com.example.webcinemabooking.repository.RoomRepository;
import com.example.webcinemabooking.repository.ShowtimeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // 1. DANH SÁCH LỊCH CHIẾU
    @GetMapping
    public String listShowtimes(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        List<Showtime> showtimes = showtimeRepository.findAll();
        model.addAttribute("showtimes", showtimes);
        return "admin/showtimes";
    }

    // 2. FORM THÊM LỊCH CHIẾU
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!checkAdmin(session)) return "redirect:/";

        model.addAttribute("showtime", new Showtime());
        // Gửi danh sách phim và phòng chiếu ra form để làm menu Dropdown (chọn thả xuống)
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/showtime-form";
    }

    // 3. LƯU LỊCH CHIẾU
    @PostMapping("/save")
    public String saveShowtime(@ModelAttribute Showtime showtime, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";

        showtimeRepository.save(showtime);
        return "redirect:/admin/showtimes";
    }

    // 4. XÓA LỊCH CHIẾU
    @GetMapping("/delete/{id}")
    public String deleteShowtime(@PathVariable("id") Long id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/";

        showtimeRepository.deleteById(id);
        return "redirect:/admin/showtimes";
    }
}