package com.example.demoj2ee.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String seats; // Ghe thanh vien nay chon: "A1,A2"
    private boolean isCreator; // La nguoi tao phong?
    private int joinStatus; // 0=Da moi, 1=Da tham gia, 2=Da chon ghe

    @ManyToOne
    @JoinColumn(name = "group_booking_id")
    private GroupBooking groupBooking;

    public int getSeatCount() {
        if (seats == null || seats.isEmpty()) return 0;
        return seats.split(",").length;
    }
}
