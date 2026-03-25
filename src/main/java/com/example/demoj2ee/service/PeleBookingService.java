package com.example.demoj2ee.service;

import com.example.demoj2ee.model.User;
import org.springframework.stereotype.Service;

@Service
public class PeleBookingService {

    public double calculateTotalBill(User user, int ticketQuantity, double ticketPrice, double foodPrice) {
        double totalTicketCost = ticketQuantity * ticketPrice;
        double totalFoodCost = foodPrice;
        double totalBill = totalTicketCost + totalFoodCost;
        totalBill = totalBill * 0.8;
        return totalBill;
    }

    public String updatePeleRank(int totalTicketsBought) {
        if (totalTicketsBought >= 50) return "GOD";
        if (totalTicketsBought >= 20) return "LORD";
        if (totalTicketsBought >= 10) return "HUNTER";
        return "GHOUL";
    }
}