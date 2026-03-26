package com.example.demoj2ee.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Ví dụ: Phòng 01, IMAX, Gold Class
    private int totalRows; // Số hàng ghế (Vd: 10 hàng)
    private int totalCols; // Số ghế mỗi hàng (Vd: 12 ghế)
}