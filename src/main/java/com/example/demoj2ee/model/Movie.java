package com.example.demoj2ee.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Column(nullable = false)
    private String title; // Tên phim (VD: Mai, Đào Phở và Piano)


    @Column(columnDefinition = "LONGTEXT") // Ép Database nhận link dài

    private String description; // Nội dung tóm tắt phim
    private String director; // Đạo diễn

    private String cast; // Dàn diễn viên chính

    private int duration; // Thời lượng phim (tính bằng phút)

    private LocalDate releaseDate; // Ngày khởi chiếu

    @Column(columnDefinition = "TEXT")
    private String posterUrl;

    private String genre;

    private String trailerUrl; // Link video Youtube

}