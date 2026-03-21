package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "foods")
@Getter
@Setter
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Ví dụ: Combo Bắp Phô Mai + 2 Nước

    private double price; // Giá tiền

    private String description; // Mô tả (Gồm 1 bắp lớn, 2 nước ngọt lớn...)

    private String imageUrl; // Link ảnh cục bắp nước
}