package com.example.demoj2ee.service;

import com.example.demoj2ee.model.User;
// DÒNG QUAN TRỌNG NHẤT ĐÂY: Mời Repository vào làm việc
import com.example.demoj2ee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // 1. HÀM XỬ LÝ ĐĂNG KÝ
    public String registerNewUser(User newUser) {
        // Kiểm tra xem tên đăng nhập đã có ai xài chưa
        User existingUser = userRepository.findByUsername(newUser.getUsername());
        if (existingUser != null) {
            return "Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác ma mị hơn.";
        }

        // Lưu xuống Database
        userRepository.save(newUser);
        return "SUCCESS";
    }

    // 2. HÀM XỬ LÝ ĐĂNG NHẬP
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        // Kiểm tra xem user có tồn tại và pass có khớp không
        if (user != null && user.getPassword().equals(password)) {
            return user; // Trả về thông tin khách hàng nếu đúng
        }
        return null; // Trả về rỗng nếu sai
    }
}