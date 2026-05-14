package com.roomate.app.entities.room;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString(exclude = "members")
@Table(name = "room")
public class RoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @NotNull
    private String address;

//    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(unique = true)
    private String roomCode;

    @NotNull
    @Column(name = "head_roommate_id")
    private String headRoommateId;

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoomMemberEntity> members = new ArrayList<>();


    public RoomEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public RoomEntity(String name, String address, String description, String roomCode, String headRoommateId, List<RoomMemberEntity> members) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.roomCode = roomCode;
        this.headRoommateId = headRoommateId;
        this.members = members != null ? members : new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RoomEntity(String name, String address, String description, String headRoommateId) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.headRoommateId = headRoommateId;
        this.createdAt = LocalDateTime.now();
        this.roomCode = UUID.randomUUID().toString();
    }
}