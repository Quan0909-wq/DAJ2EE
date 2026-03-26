package com.example.demoj2ee.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ticket_passes")
public class TicketPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vé này thuộc về ai (Người bán)
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    // THÊM CÁI NÀY: Lưu người đang bấm chốt đơn (để giam vé cho họ)
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    // Bán lại hóa đơn đặt vé nào?
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // Giá muốn pass lại (Có thể rẻ hơn giá gốc)
    private double passPrice;

    // Lời nhắn (Lý do pass)
    @Column(columnDefinition = "TEXT")
    private String reason;

    // Thông tin liên hệ
    private String contactInfo;

    // Trạng thái: AVAILABLE (Đang bán), LOCKED (Đang giam chờ tiền), SOLD (Đã bán)
    private String status = "AVAILABLE";

    private LocalDateTime createdAt = LocalDateTime.now();
}