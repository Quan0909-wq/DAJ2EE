package com.example.demoj2ee.model; // Nhớ sửa lại theo tên package của sếp

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Tên đăng nhập

    @Column(nullable = false)
    private String password; // Mật khẩu

    private String fullName; // Họ và tên
    private String email;    // Email
    private String phone;    // Số điện thoại

    // Cột này để phân biệt ADMIN và USER
    private String role = "USER";

    // Cột này để thăng hạng (Đế chế PELE)
    private int totalTicketsBought = 0;
    private String peleRank = "NEWBIE";
}