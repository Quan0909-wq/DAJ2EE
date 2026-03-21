package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Hàm này cực quan trọng để làm tính năng Đăng Nhập
    User findByUsername(String username);
}