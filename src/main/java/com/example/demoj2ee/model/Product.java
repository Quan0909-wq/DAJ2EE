package com.example.demoj2ee.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price;
    private String description;

    // THÊM BIẾN NÀY ĐỂ HỨNG DỮ LIỆU DANH MỤC TỪ GIAO DIỆN
    @Column(name = "category_id")
    private Long categoryId;

    // Constructor mặc định
    public Product() {
    }

    // Constructor 3 tham số (Giữ lại để không bị lỗi file DataSeeder)
    public Product(String name, Double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // GETTER VÀ SETTER CHO CATEGORY ID
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}