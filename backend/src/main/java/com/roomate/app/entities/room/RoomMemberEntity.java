package com.roomate.app.entities.room;

import com.roomate.app.entities.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString(exclude = {"room", "user"})
@Table(name = "room_member", indexes = {
        @Index(name = "idx_room_member_room_id", columnList = "room_id"),
        @Index(name = "idx_room_member_user_id", columnList = "user_id")
})
public class RoomMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomMemberEnum role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RoomMemberEntity() {
        this.joinedAt = LocalDateTime.now();
    }

    public RoomMemberEntity(RoomEntity room, UserEntity user, RoomMemberEnum role) {
        this.room = room;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }
}