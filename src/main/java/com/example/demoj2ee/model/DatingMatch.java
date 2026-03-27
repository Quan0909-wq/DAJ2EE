package com.example.demoj2ee.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dating_matches")
public class DatingMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @ManyToOne
    @JoinColumn(name = "profile1_id")
    private DatingProfile profile1;

    @ManyToOne
    @JoinColumn(name = "profile2_id")
    private DatingProfile profile2;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private DatingRequest request;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DatingMatch() {
    }

    public DatingMatch(User user1, User user2, DatingProfile profile1, DatingProfile profile2, DatingRequest request) {
        this.user1 = user1;
        this.user2 = user2;
        this.profile1 = profile1;
        this.profile2 = profile2;
        this.request = request;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser1() { return user1; }
    public void setUser1(User user1) { this.user1 = user1; }
    public User getUser2() { return user2; }
    public void setUser2(User user2) { this.user2 = user2; }
    public DatingProfile getProfile1() { return profile1; }
    public void setProfile1(DatingProfile profile1) { this.profile1 = profile1; }
    public DatingProfile getProfile2() { return profile2; }
    public void setProfile2(DatingProfile profile2) { this.profile2 = profile2; }
    public DatingRequest getRequest() { return request; }
    public void setRequest(DatingRequest request) { this.request = request; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getOtherUser(User me) {
        if (user1 != null && user1.getId().equals(me.getId())) return user2;
        if (user2 != null && user2.getId().equals(me.getId())) return user1;
        return null;
    }

    public DatingProfile getOtherProfile(User me) {
        if (profile1 != null && profile1.getUser() != null && profile1.getUser().getId().equals(me.getId())) return profile2;
        if (profile2 != null && profile2.getUser() != null && profile2.getUser().getId().equals(me.getId())) return profile1;
        return null;
    }
}
