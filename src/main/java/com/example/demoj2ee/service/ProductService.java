package com.example.demoj2ee.service;

import java.util.Optional;
import com.example.demoj2ee.model.Product;
import com.example.demoj2ee.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Lấy danh sách tất cả sản phẩm
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Tìm sản phẩm theo ID (Đã sửa đổi kiểu trả về thành Optional<Product>)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Thêm mới hoặc cập nhật sản phẩm
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Xóa sản phẩm theo ID
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }
    // Xóa tất cả sản phẩm (Phương thức Del All lấy điểm cộng)
    public void deleteAllProducts() {
        productRepository.truncateTable(); // Dùng lệnh truncate thay vì deleteAll
    }


}