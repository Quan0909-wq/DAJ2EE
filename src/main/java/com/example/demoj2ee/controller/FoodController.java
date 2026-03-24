package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Food;
import com.example.demoj2ee.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class FoodController {

    @Autowired
    private FoodRepository foodRepository;

    @GetMapping("/foods")
    public String showFoodMenu(Model model) {
        // Lấy tất cả bắp nước từ Database
        List<Food> foodList = foodRepository.findAll();

        // Gửi danh sách này sang giao diện HTML với tên gọi là "foods"
        model.addAttribute("foods", foodList);

        return "foods"; // Mở file foods.html
    }
}