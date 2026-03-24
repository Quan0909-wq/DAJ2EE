package com.example.demoj2ee;

import com.example.demoj2ee.model.Product;
import com.example.demoj2ee.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            // Kiểm tra xem database có trống không
            if (productRepository.count() == 0) {

                List<Product> products = Arrays.asList(
                        new Product("Lập Trình Java Căn Bản", 150000.0, "Sách hướng dẫn lập trình Java từ con số 0 cho người mới."),
                        new Product("Cấu Trúc Dữ Liệu & Giải Thuật", 125000.0, "Kiến thức nền tảng quan trọng giúp tối ưu hóa code."),
                        new Product("Thiết Kế Cơ Sở Dữ Liệu SQL", 110000.0, "Học cách thiết kế Database chuẩn và tối ưu truy vấn."),
                        new Product("Lập Trình Web với Spring Boot", 185000.0, "Xây dựng ứng dụng web Java hiện đại và mạnh mẽ."),
                        new Product("Làm Chủ JavaScript ES6", 95000.0, "Thành thạo ngôn ngữ lập trình phổ biến nhất cho Front-end."),
                        new Product("Mạng Máy Tính Căn Bản", 85000.0, "Tìm hiểu về các tầng giao thức và cách mạng máy tính vận hành."),
                        new Product("Phát Triển Ứng Dụng Di Động", 160000.0, "Lập trình app đa nền tảng với Flutter hoặc React Native."),
                        new Product("Trí Tuệ Nhân Tạo Ứng Dụng", 210000.0, "Khám phá thế giới AI, Machine Learning qua các ví dụ thực tế."),
                        new Product("Kỹ Năng Làm Việc Nhóm IT", 75000.0, "Cách giao tiếp và phối hợp hiệu quả trong các dự án phần mềm."),
                        new Product("Quản Lý Dự Án Phần Mềm", 140000.0, "Học các quy trình Agile và Scrum đang phổ biến tại các công ty."),
                        new Product("Tiếng Anh Chuyên Ngành CNTT", 120000.0, "Tài liệu học thuật chuyên sâu dành riêng cho dân lập trình."),
                        new Product("An Toàn Thông Tin Mạng", 170000.0, "Các kỹ thuật bảo vệ hệ thống khỏi các cuộc tấn công mạng.")
                );

                // Lưu toàn bộ 12 cuốn sách vào database cùng lúc
                productRepository.saveAll(products);

                System.out.println("Đã tự động thêm 12 cuốn sách mẫu vào Database thành công!");
            }
        };
    }
}