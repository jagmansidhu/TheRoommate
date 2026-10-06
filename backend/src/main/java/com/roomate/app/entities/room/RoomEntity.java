package com.roomate.app.entities.room;

import com.roomate.app.entities.ChoreEntity;
import com.roomate.app.entities.UtilityEntity;
import com.roomate.app.entities.EventEntity;
import com.roomate.app.entities.grocery.GroceryListEntity;
import com.roomate.app.entities.ledger.LedgerEntryEntity;
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

    // Members — cascade handled explicitly in service (leave/remove has its own logic)
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoomMemberEntity> members = new ArrayList<>();

    // Child data — all cascade-delete when room is deleted
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChoreEntity> chores = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UtilityEntity> utilities = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EventEntity> events = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GroceryListEntity> groceryLists = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LedgerEntryEntity> ledgerEntries = new ArrayList<>();


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