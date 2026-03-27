package com.example.demoj2ee;

import com.example.demoj2ee.model.*;
import com.example.demoj2ee.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication(scanBasePackages = "com.example.demoj2ee")
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

    @Bean
    public CommandLineRunner initRooms(RoomRepository roomRepository) {
        return args -> {
            if (roomRepository.count() > 0) return;
            Room r1 = new Room(null, "Rạp 1 - Standard", 10, 12);
            Room r2 = new Room(null, "Rạp 2 - Standard", 10, 12);
            Room r3 = new Room(null, "Rạp 3 - VIP", 8, 10);
            Room r4 = new Room(null, "Rạp 4 - IMAX", 12, 14);
            Room r5 = new Room(null, "Rạp 5 - 4DX", 8, 8);
            roomRepository.save(r1);
            roomRepository.save(r2);
            roomRepository.save(r3);
            roomRepository.save(r4);
            roomRepository.save(r5);
            System.out.println("=== Seeded 5 cinema rooms ===");
        };
    }

    @Bean
    public CommandLineRunner initMovies(MovieRepository movieRepository) {
        return args -> {
            if (movieRepository.count() > 0) return;

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Avatar: The Way of Water
            Movie m1 = new Movie();
            m1.setTitle("Avatar: The Way of Water");
            m1.setDescription("Jake Sully và Neytiri trở về hành tinh Pandora, nơi họ phải bảo vệ gia đình khỏi những mối đe dọa mới.");
            m1.setDirector("James Cameron");
            m1.setCast("Sam Worthington, Zoe Saldana, Sigourney Weaver");
            m1.setDuration(192);
            m1.setReleaseDate(LocalDate.parse("16/12/2022", df));
            m1.setGenre("Sci-Fi, Hành Động");
            m1.setPosterUrl("https://upload.wikimedia.org/wikipedia/vi/thumb/4/45/Avatar_way_of_water.jpg/220px-Avatar_way_of_water.jpg");
            m1.setTrailerUrl("https://www.youtube.com/watch?v=d9MyW72ELq0");
            movieRepository.save(m1);

            // Doraemon: Nobita và vùng đất lengan tay
            Movie m2 = new Movie();
            m2.setTitle("Doraemon: Nobita và vùng đất lengan tay");
            m2.setDescription("Nobita và bạn bè khám phá vùng đất mới với những sinh vật kỳ lạ qua cổng thần kỳ.");
            m2.setDirector("Kuga Koutarou");
            m2.setCast("Kazuya Ichii, Megumi Oohara, Fuyuhiko Nishi");
            m2.setDuration(108);
            m2.setReleaseDate(LocalDate.parse("08/12/2023", df));
            m2.setGenre("Hoạt Hình, Gia Đình");
            m2.setPosterUrl("https://upload.wikimedia.org/wikipedia/vi/thumb/3/39/Doraemon_2023_poster.jpg/220px-Doraemon_2023_poster.jpg");
            m2.setTrailerUrl("https://www.youtube.com/watch?v=nTQTK13R02E");
            movieRepository.save(m2);

            // Oppenheimer
            Movie m3 = new Movie();
            m3.setTitle("Oppenheimer");
            m3.setDescription("Câu chuyện về J. Robert Oppenheimer, nhà vật lý đứng sau việc chế tạo bom nguyên tử.");
            m3.setDirector("Christopher Nolan");
            m3.setCast("Cillian Murphy, Emily Blunt, Matt Damon");
            m3.setDuration(180);
            m3.setReleaseDate(LocalDate.parse("21/07/2023", df));
            m3.setGenre("Tiểu Sử, Chiến Tranh");
            m3.setPosterUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Oppenheimer_%28film%29.jpg/220px-Oppenheimer_%28film%29.jpg");
            m3.setTrailerUrl("https://www.youtube.com/watch?v=uYPbbksJxIg");
            movieRepository.save(m3);

            // Nha Ba Na
            Movie m4 = new Movie();
            m4.setTitle("Nhà Bà Nữ");
            m4.setDescription("Quỳnh bán hàng online với cuộc sống bình thường. Một ngày, cô gặp lại mối tình đầu và những rắc rối bắt đầu.");
            m4.setDirector("Trương Quốc Dũng");
            m4.setCast("Thái Hòa, NSND Như Quỳnh, NSUT Công Lý");
            m4.setDuration(105);
            m4.setReleaseDate(LocalDate.parse("22/01/2023", df));
            m4.setGenre("Hài, Gia Đình");
            m4.setPosterUrl("https://upload.wikimedia.org/wikipedia/vi/thumb/2/20/Nha_Ba_Na_poster.jpg/220px-Nha_Ba_Na_poster.jpg");
            m4.setTrailerUrl("https://www.youtube.com/watch?v=7xP4qZ0bNqw");
            movieRepository.save(m4);

            // Mai
            Movie m5 = new Movie();
            m5.setTitle("Mai");
            m5.setDescription("Câu chuyện về Mai, một cô gái bình dị nhưng mang trong mình vẻ đẹp khiến ai cũng phải ngoái nhìn.");
            m5.setDirector("Trấn Thành");
            m5.setCast("Trấn Thành, Phương Liên, Tuấn Trần");
            m5.setDuration(125);
            m5.setReleaseDate(LocalDate.parse("01/02/2024", df));
            m5.setGenre("Tâm Lý, Tình Cảm");
            m5.setPosterUrl("https://upload.wikimedia.org/wikipedia/vi/thumb/8/89/Mai_film_poster.jpg/220px-Mai_film_poster.jpg");
            m5.setTrailerUrl("https://www.youtube.com/watch?v=7kSa8cRbx8I");
            movieRepository.save(m5);

            // Avengers: Endgame
            Movie m6 = new Movie();
            m6.setTitle("Avengers: Endgame");
            m6.setDescription("Các siêu anh hùng Avengers hợp sức chiến đấu chống lại Thanos để lấy lại vũ trụ.");
            m6.setDirector("Anthony Russo, Joe Russo");
            m6.setCast("Robert Downey Jr., Chris Evans, Scarlett Johansson");
            m6.setDuration(181);
            m6.setReleaseDate(LocalDate.parse("26/04/2019", df));
            m6.setGenre("Hành Động, Siêu Anh Hùng");
            m6.setPosterUrl("https://upload.wikimedia.org/wikipedia/vi/thumb/0/0d/Avengers_Endgame_poster.jpg/220px-Avengers_Endgame_poster.jpg");
            m6.setTrailerUrl("https://www.youtube.com/watch?v=TcMBFSGVi1c");
            movieRepository.save(m6);

            // Trạm Rửa Mắt
            Movie m7 = new Movie();
            m7.setTitle("Trạm Rửa Mắt");
            m7.setDescription("Chuyện tình đồng giới trong xã hội Việt Nam, khi một chàng trai trẻ gặp lại tình đầu.");
            m7.setDirector("Nguyen Quang Dzung");
            m7.setCast("Hieuvannguyen, Thang Nguyen");
            m7.setDuration(104);
            m7.setReleaseDate(LocalDate.parse("14/07/2023", df));
            m7.setGenre("Tâm Lý, Tình Cảm");
            m7.setPosterUrl("https://upload.wikimedia.org/wikipedia/vi/thumb/a/a5/TramRuaMat.jpg/220px-TramRuaMat.jpg");
            m7.setTrailerUrl("https://www.youtube.com/watch?v=Hu_1hq1bJVs");
            movieRepository.save(m7);

            // Demon Slayer: To the Swordsmith Village
            Movie m8 = new Movie();
            m8.setTitle("Demon Slayer: Pháo Đài Vô Tận");
            m8.setDescription("Tanjiro và Nezuko đối mặt với các demon mới tại làng kiếm sĩ trong cuộc chiến chống lại Muzan.");
            m8.setDirector("Sotozaki Haruo");
            m8.setCast("Hanae Natsuki, Shimatani Hiro, Koyaguchi Tomoyuki");
            m8.setDuration(110);
            m8.setReleaseDate(LocalDate.parse("03/03/2023", df));
            m8.setGenre("Hoạt Hình, Hành Động");
            m8.setPosterUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/2/26/Demon_Slayer_-_Kimetsu_no_Yaiba_-_To_the_Swordsmith_Village.jpg/220px-Demon_Slayer_-_Kimetsu_no_Yaiba_-_To_the_Swordsmith_Village.jpg");
            m8.setTrailerUrl("https://www.youtube.com/watch?v=5K-7J4W6sRw");
            movieRepository.save(m8);

            // Deadpool & Wolverine
            Movie m9 = new Movie();
            m9.setTitle("Deadpool & Wolverine");
            m9.setDescription("Deadpool tham gia Time Variance Authority để tìm kiếm Wolverine từ một vũ trụ khác nhằm cứu vũ trụ của mình.");
            m9.setDirector("Shawn Levy");
            m9.setCast("Ryan Reynolds, Hugh Jackman, Emma Corrin");
            m9.setDuration(127);
            m9.setReleaseDate(LocalDate.parse("26/07/2024", df));
            m9.setGenre("Hài, Hành Động, Siêu Anh Hùng");
            m9.setPosterUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Deadpool_%26_Wolverine_poster.jpg/220px-Deadpool_%26_Wolverine_poster.jpg");
            m9.setTrailerUrl("https://www.youtube.com/watch?v=73_1biulkYk");
            movieRepository.save(m9);

            //Inside Out 2
            Movie m10 = new Movie();
            m10.setTitle("Nội Tâm 2 (Inside Out 2)");
            m10.setDescription("Riley bước vào tuổi dậy thì với những cảm xúc mới xuất hiện: lo lắng, hổ thẹn, chán nản, và ghen tị.");
            m10.setDirector("Kelsey Mann");
            m10.setCast("Amy Poehler, Maya Hawke, Kensington Tallman");
            m10.setDuration(96);
            m10.setReleaseDate(LocalDate.parse("14/06/2024", df));
            m10.setGenre("Hoạt Hình, Phiêu Lưu");
            m10.setPosterUrl("https://upload.wikimedia.org/wikipedia/vi/thumb/f/f5/Inside_Out_2_poster.jpg/220px-Inside_Out_2_poster.jpg");
            m10.setTrailerUrl("https://www.youtube.com/watch?v=LEjhY15eCx0");
            movieRepository.save(m10);

            System.out.println("=== Seeded 10 movies ===");
        };
    }

    @Bean
    public CommandLineRunner initShowtimes(
            MovieRepository movieRepository,
            RoomRepository roomRepository,
            ShowtimeRepository showtimeRepository) {
        return args -> {
            if (showtimeRepository.count() > 0) return;

            var movies = movieRepository.findAll();
            var rooms = roomRepository.findAll();
            if (movies.isEmpty() || rooms.isEmpty()) {
                System.out.println("=== Skipping showtimes: movies or rooms empty ===");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Showtime 1: Avatar - Rap 4 IMAX - 14:00
            Showtime s1 = new Showtime();
            s1.setMovie(movies.get(0));
            s1.setRoom(rooms.get(3)); // Rap 4 IMAX
            s1.setStartTime(now.plusDays(1).withHour(14).withMinute(0));
            s1.setPrice(120000);
            showtimeRepository.save(s1);

            // Showtime 2: Doraemon - Rap 1 Standard - 09:00
            Showtime s2 = new Showtime();
            s2.setMovie(movies.get(1));
            s2.setRoom(rooms.get(0)); // Rap 1 Standard
            s2.setStartTime(now.plusDays(1).withHour(9).withMinute(0));
            s2.setPrice(60000);
            showtimeRepository.save(s2);

            // Showtime 3: Oppenheimer - Rap 3 VIP - 19:30
            Showtime s3 = new Showtime();
            s3.setMovie(movies.get(2));
            s3.setRoom(rooms.get(2)); // Rap 3 VIP
            s3.setStartTime(now.plusDays(2).withHour(19).withMinute(30));
            s3.setPrice(150000);
            showtimeRepository.save(s3);

            // Showtime 4: Mai - Rap 2 Standard - 17:00
            Showtime s4 = new Showtime();
            s4.setMovie(movies.get(4));
            s4.setRoom(rooms.get(1)); // Rap 2 Standard
            s4.setStartTime(now.plusDays(2).withHour(17).withMinute(0));
            s4.setPrice(70000);
            showtimeRepository.save(s4);

            // Showtime 5: Deadpool & Wolverine - Rap 5 4DX - 21:00
            Showtime s5 = new Showtime();
            s5.setMovie(movies.get(8));
            s5.setRoom(rooms.get(4)); // Rap 5 4DX
            s5.setStartTime(now.plusDays(3).withHour(21).withMinute(0));
            s5.setPrice(180000);
            showtimeRepository.save(s5);

            System.out.println("=== Seeded 5 showtimes ===");
        };
    }

    @Bean
    public CommandLineRunner initTicketPasses(UserRepository userRepository, BookingRepository bookingRepository, TicketPassRepository ticketPassRepository) {
        return args -> {
            if (ticketPassRepository.count() > 0) return;

            var users = userRepository.findAll();
            if (users.isEmpty()) {
                System.out.println("=== Skipping ticket passes: no users ===");
                return;
            }

            User seller = users.get(0);

            TicketPass p1 = new TicketPass();
            p1.setSeller(seller);
            p1.setPassPrice(85000);
            p1.setReason("Mình có 2 vé thừa, ai cần liên hệ nha! Phim Avatar rất hay.");
            p1.setContactInfo("Zalo: 0912345678");
            p1.setStatus("AVAILABLE");
            ticketPassRepository.save(p1);

            TicketPass p2 = new TicketPass();
            p2.setSeller(seller);
            p2.setPassPrice(70000);
            p2.setReason("Vé Doraemon cho bé yêu không đi được, bán lại giá gốc.");
            p2.setContactInfo("Facebook: PeleCinema");
            p2.setStatus("AVAILABLE");
            ticketPassRepository.save(p2);

            TicketPass p3 = new TicketPass();
            p3.setSeller(seller);
            p3.setPassPrice(120000);
            p3.setReason("Vé IMAX Oppenheimer, thừa 1 vé. Ghế đẹp giữa rạp.");
            p3.setContactInfo("Zalo: 0912345678");
            p3.setStatus("AVAILABLE");
            ticketPassRepository.save(p3);

            TicketPass p4 = new TicketPass();
            p4.setSeller(seller);
            p4.setPassPrice(50000);
            p4.setReason("Bán gấp vé Mai - phim Trấn Thành, vé Standard.");
            p4.setContactInfo("Instagram: @pele cinema");
            p4.setStatus("AVAILABLE");
            ticketPassRepository.save(p4);

            TicketPass p5 = new TicketPass();
            p5.setSeller(seller);
            p5.setPassPrice(180000);
            p5.setReason("Vé 4DX Deadpool & Wolverine - trải nghiệm cực phê! Thừa 1 vé.");
            p5.setContactInfo("Zalo: 0912345678");
            p5.setStatus("AVAILABLE");
            ticketPassRepository.save(p5);

            System.out.println("=== Seeded 5 ticket passes ===");
        };
    }

    @Bean
    public CommandLineRunner initDatingProfiles(UserRepository userRepository, DatingProfileRepository datingProfileRepository) {
        return args -> {
            if (datingProfileRepository.count() > 0) return;

            var users = userRepository.findAll();
            if (users.isEmpty()) return;

            // Create dating profile for admin as a demo
            if (users.size() >= 1) {
                User admin = users.get(0);
                DatingProfile dp = new DatingProfile(admin);
                dp.setDisplayName("Admin Pele");
                dp.setAge(25);
                dp.setHeight(175.0);
                dp.setHometown("TP. Hồ Chí Minh");
                dp.setMaritalStatus("SINGLE");
                dp.setBio("Chào mọi người! Mình là fan phim kinh điển. Rất vui được kết bạn!");
                dp.setAvatarUrl("https://ui-avatars.com/api/?name=Admin+Pele&background=E50914&color=fff&size=256");
                dp.setActive(true);
                datingProfileRepository.save(dp);
                System.out.println("=== Seeded demo dating profile for admin ===");
            }
        };
    }

}
