package com.example.demoj2ee.repository; // Kiểm tra đúng package của sếp nha

import com.example.demoj2ee.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    // JpaRepository sẽ tự có hàm findAll(), giúp sếp hết lỗi 'Cannot resolve method'
}