package com.example.demoj2ee.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Tên đăng nhập (không được trùng)

    @Column(nullable = false)
    private String password; // Mật khẩu

    @Column(nullable = false, unique = true)
    private String email; // Email (để sau này làm chức năng gửi Gmail xác nhận)

    private String fullName; // Họ và tên thật

    private String phone; // Số điện thoại

    // Mặc định ai đăng ký mới cũng là khách hàng (USER).
    // Lát nữa mình vào Database sửa 1 tài khoản thành ADMIN để cậu quản lý rạp.
    private String role = "USER";
    @Column(columnDefinition = "LONGTEXT")
    private String avatar;
    @Column(name = "total_tickets_bought", columnDefinition = "integer default 0")
    private int totalTicketsBought = 0; // Đếm tổng số vé đã mua

    @Column(name = "pele_rank")
    private String peleRank = "GHOUL"; // Mặc định đăng ký mới là Tân Binh (Ghoul)

    // Sếp nhớ tạo Getter và Setter cho 2 biến này nhé!
    public int getTotalTicketsBought() { return totalTicketsBought; }
    public void setTotalTicketsBought(int totalTicketsBought) { this.totalTicketsBought = totalTicketsBought; }

    public String getPeleRank() { return peleRank; }
    public void setPeleRank(String peleRank) { this.peleRank = peleRank; }
}