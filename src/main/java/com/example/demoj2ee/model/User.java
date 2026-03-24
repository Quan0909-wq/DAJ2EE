package com.example.demoj2ee.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Các trường quan trọng nhất để nộp bài
    private String name;
    private String email;
    private String password;
    private String role;
    private String dateOfBirth; // Để kiểu String cho chắc chắn không bị lỗi định dạng
    private String address;
    private String phoneNumber;

    // Giữ lại các trường cũ của project đặt vé để tránh lỗi liên đới
    private String username;
    private String fullName;
    private String phone;
    private String peleRank;

    public User() {}

    // TỰ TAY VIẾT GETTER/SETTER (KHÔNG DÙNG LOMBOK ĐỂ TRÁNH LỖI)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @PrePersist
    protected void onCreate() {
        if (this.username == null) {
            this.username = "user_" + System.currentTimeMillis();
        }
    }
}