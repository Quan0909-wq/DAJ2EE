package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GroupBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomCode; // Ma phong 6 ky tu (VD: "ABC123")

    private int status; // 0=Cho, 1=Dang chon ghe, 2=Da thanh toan, 3=Het han

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator; // Nguoi tao phong

    @OneToMany(mappedBy = "groupBooking", cascade = CascadeType.ALL)
    private List<GroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "groupBooking", cascade = CascadeType.ALL)
    private List<GroupPayment> payments = new ArrayList<>();

    private String allSeats; // Tong ghe: "A1,A2,B3,B4"
    private double originalAmount; // Tong tien chua giam
    private double discountPercent; // % giam gia
    private double finalAmount; // Tong tien sau giam
    private int paymentMode; // 0=Mot nguoi tra, 1=Chia deu
    private int confirmedCount; // So nguoi da xac nhan thanh toan

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (expiresAt == null) expiresAt = createdAt.plusMinutes(30);
        if (status == 0) status = 0;
    }

    // Lay muc giam gia theo so ghe
    public static double getDiscountBySeats(int seatCount) {
        if (seatCount >= 30) return 0.20;
        if (seatCount >= 20) return 0.15;
        if (seatCount >= 10) return 0.10;
        return 0.0;
    }

    // Lay text muc giam gia
    public static String getDiscountText(int seatCount) {
        if (seatCount >= 30) return "20%";
        if (seatCount >= 20) return "15%";
        if (seatCount >= 10) return "10%";
        return "0%";
    }

    public int getTotalSeats() {
        if (allSeats == null || allSeats.isEmpty()) return 0;
        return allSeats.split(",").length;
    }
}
