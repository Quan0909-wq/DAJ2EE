package com.example.webcinemabooking.service;

import com.example.webcinemabooking.model.User;
import org.springframework.stereotype.Service;

@Service
public class PeleBookingService {

    // Hàm cập nhật hạng (giữ nguyên)
    public String updatePeleRank(int totalTickets) {
        if (totalTickets >= 10) return "LORD";
        else if (totalTickets >= 5) return "HUNTER";
        else return "GHOUL";
    }

    // HÀM TÍNH TIỀN ĐÃ FIX LỖI NULL
    public double calculateTotalBill(User user, int ticketQuantity, double ticketPrice, double foodPrice) {
        double totalTicketCost = ticketQuantity * ticketPrice;
        double totalFoodCost = foodPrice;

        // 1. Khuyến mãi vé nhóm (Áp dụng cho mọi người, kể cả khách chưa đăng nhập)
        if (ticketQuantity >= 10) {
            totalTicketCost = totalTicketCost * 0.95;
        }

        // 2. Khuyến mãi hạng LORD (PHẢI CHECK USER KHÁC NULL MỚI CHẠY)
        // Nếu user null (chưa đăng nhập) thì bỏ qua bước này, không báo lỗi nữa
        if (user != null && "LORD".equals(user.getPeleRank())) {
            totalFoodCost = totalFoodCost * 0.90;
        }

        return totalTicketCost + totalFoodCost;
    }
}