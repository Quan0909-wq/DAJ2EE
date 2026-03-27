package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Giảm theo số tiền cố định (VD: 50000 = giảm 50,000đ). Set 0 nếu dùng %. */
    private double discountAmount;

    /** Giảm theo phần trăm (VD: 20 = giảm 20%). Set 0 nếu dùng số tiền. */
    private int discountPercent;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active = true;

    /** Suất chiếu áp dụng (null = áp dụng mọi suất chiếu). */
    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    /** Tính số tiền được giảm dựa trên giá gốc. */
    public double calculateDiscount(double originalPrice) {
        if (!active || (expiresAt != null && expiresAt.isBefore(LocalDateTime.now()))) {
            return 0;
        }
        if (discountAmount > 0) {
            return Math.min(discountAmount, originalPrice);
        }
        if (discountPercent > 0) {
            return originalPrice * discountPercent / 100.0;
        }
        return 0;
    }
}
