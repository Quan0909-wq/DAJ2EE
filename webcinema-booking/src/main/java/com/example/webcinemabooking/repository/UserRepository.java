package com.example.webcinemabooking.repository;

import com.example.webcinemabooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 2 hàm này cực kỳ quan trọng để lát nữa làm chức năng Đăng Nhập
    User findByUsername(String username);
    User findByEmail(String email);
    Long countByRole(String role); // Đếm số lượng khách hàng theo quyền
}