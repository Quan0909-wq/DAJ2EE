package com.example.webcinemabooking.model;

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

    // Liên kết với cái vé đang bị kiện
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // LIÊN KẾT THẲNG VỚI BẢNG USER CỦA SẾP
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String messageContent; // Nội dung tin nhắn

    private String evidenceImageUrl; // Đường dẫn ảnh bằng chứng (Nếu có upload)

    private LocalDateTime sentAt = LocalDateTime.now(); // Thời gian gửi
}