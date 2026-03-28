package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Dùng cho chức năng Đăng Nhập
    User findByUsername(String username);

    // Dùng cho chức năng Đăng Ký
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // THÊM MỚI: tìm kiếm user theo username, fullName hoặc email
    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String fullName, String email
    );
}