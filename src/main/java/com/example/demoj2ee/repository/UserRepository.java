package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Dùng cho chức năng Đăng Nhập
    User findByUsername(String username);

    // Dùng cho chức năng Đăng Ký (Kiểm tra xem đã có ai dùng chưa)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}