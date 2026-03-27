package com.example.demoj2ee.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dating_notifications")
public class DatingNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "dating_request_id")
    private DatingRequest datingRequest;

    @ManyToOne
    @JoinColumn(name = "dating_match_id")
    private DatingMatch datingMatch;

    @Column(nullable = false)
    private String type; // REQUEST_SENT, REQUEST_RECEIVED, MATCHED, NEW_MESSAGE

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "is_shown", nullable = false)
    private boolean isShown = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DatingNotification() {
    }

    public DatingNotification(User toUser, User fromUser, String type) {
        this.toUser = toUser;
        this.fromUser = fromUser;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }
    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }
    public DatingRequest getDatingRequest() { return datingRequest; }
    public void setDatingRequest(DatingRequest datingRequest) { this.datingRequest = datingRequest; }
    public DatingMatch getDatingMatch() { return datingMatch; }
    public void setDatingMatch(DatingMatch datingMatch) { this.datingMatch = datingMatch; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public boolean isShown() { return isShown; }
    public void setShown(boolean shown) { isShown = shown; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
