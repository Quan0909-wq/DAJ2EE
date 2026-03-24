package com.example.demoj2ee.service;

import com.example.demoj2ee.model.User;
import org.springframework.stereotype.Service;

@Service
public class PeleBookingService {

    // Đã xóa hàm updatePeleRank vì class User không còn lưu hạng này nữa.

    // Hàm tính tiền đã được tối ưu và fix lỗi
    public double calculateTotalBill(User user, int ticketQuantity, double ticketPrice, double foodPrice) {
        double totalTicketCost = ticketQuantity * ticketPrice;
        double totalFoodCost = foodPrice;

        double totalBill = totalTicketCost + totalFoodCost;

        // Trợ giá sinh viên: Giảm trực tiếp 20% trên tổng hóa đơn (Vé + Bắp nước)
        totalBill = totalBill * 0.8;

        return totalBill;
    }
}