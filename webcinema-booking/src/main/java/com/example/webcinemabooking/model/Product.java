package com.example.webcinemabooking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Ví dụ: Combo Bắp + 2 Nước
    private double price; // Ví dụ: 99000
    private String imageUrl; // Link ảnh bắp nước cho đẹp
}