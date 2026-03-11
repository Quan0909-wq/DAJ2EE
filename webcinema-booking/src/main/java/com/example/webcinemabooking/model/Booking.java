package com.example.webcinemabooking.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    private String customerName;
    private String customerEmail;
    private String seatNumbers; // Lưu dạng chuỗi: "A1, A2, A3"
    private double totalAmount;
    private LocalDateTime bookingTime;
    private String customerPhone; // Thêm dòng này
}