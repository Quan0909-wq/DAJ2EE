package com.example.demoj2ee.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dating_requests")
public class DatingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @ManyToOne
    @JoinColumn(name = "from_profile_id")
    private DatingProfile fromProfile;

    @ManyToOne
    @JoinColumn(name = "to_profile_id")
    private DatingProfile toProfile;

    // Trạng thái: PENDING, ACCEPTED, REJECTED
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DatingRequest() {
    }

    public DatingRequest(User fromUser, User toUser, DatingProfile fromProfile, DatingProfile toProfile) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.fromProfile = fromProfile;
        this.toProfile = toProfile;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }
    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }
    public DatingProfile getFromProfile() { return fromProfile; }
    public void setFromProfile(DatingProfile fromProfile) { this.fromProfile = fromProfile; }
    public DatingProfile getToProfile() { return toProfile; }
    public void setToProfile(DatingProfile toProfile) { this.toProfile = toProfile; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
