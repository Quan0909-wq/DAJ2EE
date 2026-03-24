package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtimes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie; // Phim nào chiếu?

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room; // Phòng nào?

    private LocalDateTime startTime; // Mấy giờ bắt đầu?
    private double price; // Giá vé suất này (Vd: 80000)
}