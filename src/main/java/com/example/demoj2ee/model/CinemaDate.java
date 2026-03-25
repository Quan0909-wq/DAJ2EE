package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cinema_dates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CinemaDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "host_user_id")
    private User hostUser;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    @Column(columnDefinition = "TEXT")
    private String bioMessage;

    @Column(nullable = false)
    private String status = "PENDING";

    @ManyToOne
    @JoinColumn(name = "matched_user_id")
    private User matchedUser;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "cinemaDate", cascade = CascadeType.ALL)
    private List<MatchRequest> matchRequests;

    @ManyToMany
    @JoinTable(
            name = "cinema_date_likes",
            joinColumns = @JoinColumn(name = "cinema_date_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedByUsers = new HashSet<>();

    public boolean isLikedBy(User user) {
        if (user == null) return false;
        for (User u : likedByUsers) {
            if (u.getId().equals(user.getId())) return true;
        }
        return false;
    }
}
