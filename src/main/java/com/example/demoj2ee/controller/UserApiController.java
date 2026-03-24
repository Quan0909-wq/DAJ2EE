package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.User;
import com.example.demoj2ee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserApiController {

    @Autowired
    private UserService userService;

    // Lấy danh sách người dùng
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Thêm mới người dùng
    @PostMapping
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    // Cập nhật người dùng
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userService.getUserById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            // Chỉ cập nhật password nếu có gửi password mới lên
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty() && !userDetails.getPassword().equals("defaultPassword")) {
                user.setPassword(userDetails.getPassword());
            }
            user.setRole(userDetails.getRole());
            user.setDateOfBirth(userDetails.getDateOfBirth());
            user.setAddress(userDetails.getAddress());
            user.setPhoneNumber(userDetails.getPhoneNumber());

            return ResponseEntity.ok(userService.addUser(user));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Xóa người dùng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/all")
    @CrossOrigin("*")
    public ResponseEntity<Void> deleteAll() {
        userService.deleteAllUsers();
        return ResponseEntity.noContent().build();
    }
}