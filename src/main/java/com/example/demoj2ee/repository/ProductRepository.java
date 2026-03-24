package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE products", nativeQuery = true)
    void truncateTable();
    // JpaRepository đã lo sẵn các lệnh tương tác với database (thêm, sửa, xóa, tìm kiếm)
}
