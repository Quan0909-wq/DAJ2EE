package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, RESOLVED, REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    @Column(columnDefinition = "LONGTEXT")
    private String adminReply;

    // THÊM MỚI: loại xử lý
    @Column(name = "resolution_type")
    private String resolutionType;
    // CHANGE_SEAT, CHANGE_SHOWTIME, CHANGE_MOVIE, CANCEL_BOOKING, REJECTED, OTHER

    // THÊM MỚI: khách còn cần thao tác tiếp không
    @Column(name = "customer_action_required")
    private boolean customerActionRequired = false;

    // THÊM MỚI: khách đã xử lý bước tiếp theo chưa
    @Column(name = "customer_action_done")
    private boolean customerActionDone = false;
}