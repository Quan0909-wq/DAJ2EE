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

    // ========== DATING PROFILE ==========
    private Integer age; // Tuổi: 18-99
    private Double height; // Chiều cao (cm): 100-250
    private String hometown; // Quê quán
    private String relationshipStatus; // Tình trạng: DOC_THAN, HEN_HO, PHUC_TAP
    private String datingBio; // Giới thiệu bản thân

    @Column(columnDefinition = "LONGTEXT")
    private String datingAvatar; // Ảnh đại diện dating (bắt buộc)
    @Column(columnDefinition = "LONGTEXT")
    private String datingPhoto1; // Ảnh 1
    @Column(columnDefinition = "LONGTEXT")
    private String datingPhoto2; // Ảnh 2
    @Column(columnDefinition = "LONGTEXT")
    private String datingPhoto3; // Ảnh 3

    private boolean datingProfileComplete = false; // Đã hoàn thiện hồ sơ dating chưa

    public boolean isDatingProfileComplete() {
        return datingAvatar != null && !datingAvatar.isBlank()
            && age != null && age >= 18
            && height != null && height >= 100
            && hometown != null && !hometown.isBlank()
            && relationshipStatus != null && !relationshipStatus.isBlank()
            && datingBio != null && !datingBio.isBlank();
    }
}