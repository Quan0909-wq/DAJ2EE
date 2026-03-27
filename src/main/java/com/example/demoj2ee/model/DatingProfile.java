package com.example.demoj2ee.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dating_profiles")
public class DatingProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Null cho đến khi người dùng điền form (tránh lỗi INSERT khi tạo hồ sơ mới). */
    @Column(name = "display_name")
    private String displayName;

    @Column
    private Integer age; // Tuổi (18-99)

    @Column
    private Double height; // Chiều cao (cm, 100-250)

    @Column(name = "hometown")
    private String hometown; // Quê quán

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio; // Giới thiệu bản thân

    @Column(name = "marital_status")
    private String maritalStatus; // Tình trạng hôn nhân: SINGLE, MARRIED, DIVORCED, WIDOWED

    @Column(name = "avatar_url", columnDefinition = "LONGTEXT")
    private String avatarUrl; // Ảnh đại diện (bắt buộc khi hoàn thiện hồ sơ)

    @Column(name = "photo_1", columnDefinition = "LONGTEXT")
    private String photo1; // Ảnh 1 (tối đa 4)

    @Column(name = "photo_2", columnDefinition = "LONGTEXT")
    private String photo2; // Ảnh 2

    @Column(name = "photo_3", columnDefinition = "LONGTEXT")
    private String photo3; // Ảnh 3

    @Column(name = "photo_4", columnDefinition = "LONGTEXT")
    private String photo4; // Ảnh 4

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false; // Đã hoàn thiện hồ sơ chưa

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DatingProfile() {
    }

    public DatingProfile(User user) {
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isProfileComplete() {
        return displayName != null && !displayName.isBlank()
            && age != null && age >= 18 && age <= 99
            && height != null && height >= 100 && height <= 250
            && hometown != null && !hometown.isBlank()
            && maritalStatus != null && !maritalStatus.isBlank()
            && avatarUrl != null && !avatarUrl.isBlank();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    public String getHometown() { return hometown; }
    public void setHometown(String hometown) { this.hometown = hometown; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPhoto1() { return photo1; }
    public void setPhoto1(String photo1) { this.photo1 = photo1; }
    public String getPhoto2() { return photo2; }
    public void setPhoto2(String photo2) { this.photo2 = photo2; }
    public String getPhoto3() { return photo3; }
    public void setPhoto3(String photo3) { this.photo3 = photo3; }
    public String getPhoto4() { return photo4; }
    public void setPhoto4(String photo4) { this.photo4 = photo4; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String[] getAllPhotos() {
        java.util.List<String> photos = new java.util.ArrayList<>();
        if (avatarUrl != null && !avatarUrl.isBlank()) photos.add(avatarUrl);
        if (photo1 != null && !photo1.isBlank()) photos.add(photo1);
        if (photo2 != null && !photo2.isBlank()) photos.add(photo2);
        if (photo3 != null && !photo3.isBlank()) photos.add(photo3);
        if (photo4 != null && !photo4.isBlank()) photos.add(photo4);
        return photos.toArray(new String[0]);
    }
}
