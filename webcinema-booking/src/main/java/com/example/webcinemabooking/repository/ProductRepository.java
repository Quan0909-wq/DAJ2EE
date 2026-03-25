package com.example.webcinemabooking.repository;

import com.example.webcinemabooking.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Chỉ cần kế thừa JpaRepository là sếp có sẵn các hàm:
    // findAll() - Lấy tất cả bắp nước
    // findById() - Tìm theo mã
    // save() - Thêm hoặc cập nhật sản phẩm
}