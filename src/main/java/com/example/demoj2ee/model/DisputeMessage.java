package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispute_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DisputeMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String messageContent;

    private String evidenceImageUrl;

    private LocalDateTime sentAt = LocalDateTime.now();
}
