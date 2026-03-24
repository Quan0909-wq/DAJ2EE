package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GroupPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount; // So tien can tra
    private String status; // PENDING/PAID/TIMEOUT
    private String qrData; // Du lieu QR mock
    private String paymentMethod; // Phuong thuc thanh toan (VNPay mock, etc.)
    private LocalDateTime paidAt;

    @ManyToOne
    @JoinColumn(name = "group_booking_id")
    private GroupBooking groupBooking;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private GroupMember member;
}
