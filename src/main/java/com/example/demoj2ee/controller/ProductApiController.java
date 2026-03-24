package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.Product;
import com.example.demoj2ee.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    // Lấy danh sách Sách
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // Thêm Sách mới
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    // Sửa/Cập nhật Sách
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productService.getProductById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setDescription(productDetails.getDescription());

            // DÒNG QUAN TRỌNG NHẤT: Lưu lại ID Danh Mục khi cập nhật!
            product.setCategoryId(productDetails.getCategoryId());

            return ResponseEntity.ok(productService.addProduct(product));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Xóa một Sách
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    // Xóa TẤT CẢ Sách (Lấy điểm cộng)
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllProducts() {
        productService.deleteAllProducts();
        return ResponseEntity.noContent().build();
    }
}