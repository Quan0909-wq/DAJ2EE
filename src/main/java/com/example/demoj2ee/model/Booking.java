package com.example.demoj2ee.model;

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
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String seatNumbers;
    private double totalAmount;
    private LocalDateTime bookingTime;

    private String status;

    // THÊM MỚI: quyền thao tác sau khi khiếu nại được chấp nhận
    @Column(name = "allow_seat_change")
    private boolean allowSeatChange = false;

    @Column(name = "allow_showtime_change")
    private boolean allowShowtimeChange = false;

    @Column(name = "allow_movie_change")
    private boolean allowMovieChange = false;

    @Column(name = "allow_cancel_booking")
    private boolean allowCancelBooking = false;

    @Column(name = "dispute_note", columnDefinition = "LONGTEXT")
    private String disputeNote;
}