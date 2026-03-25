package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cinema_date_id")
    private CinemaDate cinemaDate;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String hostReply;

    private boolean isLikedByHost = false;

    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();
}
