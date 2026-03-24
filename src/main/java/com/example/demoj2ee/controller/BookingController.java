package com.example.demoj2ee.controller; // Nhớ đổi đúng tên thư mục demoj2ee của sếp nha

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookingController {

    // 1. Mở trang chọn ghế (Cái giao diện rạp phim có màn hình cong)
    @GetMapping("/book-seat")
    public String showSeatMap(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        // Tạm thời hiển thị phim Dune 2
        model.addAttribute("movieName", "Dune: Hành Tinh Cát 2");
        model.addAttribute("roomName", "Phòng chiếu số 3 (IMAX)");
        return "book-seat";
    }

    // 2. Xử lý khi khách bấm nút "Tiếp tục chọn bắp nước"
    @PostMapping("/process-seats")
    public String processSeats(@RequestParam("selectedSeats") String selectedSeats,
                               @RequestParam("totalTicketPrice") int totalTicketPrice,
                               HttpSession session) {

        // Nhét ghế và tiền vé vào balo (Session) để mang sang trang Checkout
        session.setAttribute("selectedSeats", selectedSeats);
        session.setAttribute("ticketPrice", totalTicketPrice);
        session.setAttribute("movieName", "Dune: Hành Tinh Cát 2");

        // Đá khách sang trang Thanh toán / Mua bắp nước
        return "redirect:/checkout";
    }
}