package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data // Tui thấy sếp có cài Lombok rồi nên xài luôn cho lẹ, khỏi viết Getter/Setter
@Table(name = "foods")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;
    private String description;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}