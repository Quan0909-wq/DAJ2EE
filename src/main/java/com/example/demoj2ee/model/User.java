package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Tên đăng nhập

    @Column(nullable = false)
    private String password; // Mật khẩu

    @Column(nullable = false, unique = true)
    private String email; // Email

    private String fullName; // Họ và tên thật

    private String phone; // Số điện thoại

    private String role = "USER";

    @Column(columnDefinition = "LONGTEXT")
    private String avatar;

    @Column(name = "total_tickets_bought", columnDefinition = "integer default 0")
    private int totalTicketsBought = 0;

    @Column(name = "pele_rank")
    private String peleRank = "GHOUL";

    // THÊM MỚI: trạng thái tài khoản
    @Column(name = "active")
    private boolean active = true;

    public int getTotalTicketsBought() {
        return totalTicketsBought;
    }

    public void setTotalTicketsBought(int totalTicketsBought) {
        this.totalTicketsBought = totalTicketsBought;
    }

    public String getPeleRank() {
        return peleRank;
    }

    public void setPeleRank(String peleRank) {
        this.peleRank = peleRank;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}