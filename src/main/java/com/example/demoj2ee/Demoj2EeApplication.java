package com.example.demoj2ee;

import com.example.demoj2ee.model.Room;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.RoomRepository;
import com.example.demoj2ee.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Demoj2EeApplication {

    public static void main(String[] args) {
        SpringApplication.run(Demoj2EeApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {
            User admin = userRepository.findByUsername("admin");
            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@pele.com");
                admin.setFullName("Quan Tri Vien");
                admin.setRole("ADMIN");
                System.out.println("=== Admin account created: admin / admin123 ===");
            }
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("=== Admin password & role ensured: admin / admin123 ===");
        };
    }

    /**
     * Dam bao co it nhat mot phong chieu de admin them suat chieu (tranh dropdown trong).
     */
    @Bean
    public CommandLineRunner initRooms(RoomRepository roomRepository) {
        return args -> {
            if (roomRepository.count() > 0) {
                return;
            }
            roomRepository.save(new Room(null, "Phòng 1 - Standard", 10, 12));
            roomRepository.save(new Room(null, "Phòng 2 - Standard", 10, 12));
            roomRepository.save(new Room(null, "Phòng 3 - Gold Class", 8, 10));
            System.out.println("=== Seeded default cinema rooms (Phong 1, 2, 3) ===");
        };
    }

}
