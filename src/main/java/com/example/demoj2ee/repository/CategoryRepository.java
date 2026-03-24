package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Hàm xóa sạch và reset ID của Danh mục về 1
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE categories", nativeQuery = true)
    void truncateTable();
}