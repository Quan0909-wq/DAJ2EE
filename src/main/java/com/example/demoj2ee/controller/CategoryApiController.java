package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Category;
import com.example.demoj2ee.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*") // Cực kỳ quan trọng: Cho phép giao diện ở port 5500 gọi API ở port 8080
public class CategoryApiController {

    @Autowired
    private CategoryService categoryService;

    // Lấy danh sách
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // Thêm mới
    @PostMapping
    public Category addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    // Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        return categoryService.getCategoryById(id).map(category -> {
            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());
            return ResponseEntity.ok(categoryService.addCategory(category));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/all")
    @CrossOrigin("*")
    public ResponseEntity<Void> deleteAll() {
        categoryService.deleteAllCategories();
        return ResponseEntity.noContent().build();
    }
}