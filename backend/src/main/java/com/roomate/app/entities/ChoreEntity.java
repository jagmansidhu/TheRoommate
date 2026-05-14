package com.roomate.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name = "idx_chore_room_id", columnList = "room_id"),
        @Index(name = "idx_chore_room_member_id", columnList = "room_member_id"),
        @Index(name = "idx_chore_room_due", columnList = "room_id, due_at")
})
public class ChoreEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    private String choreName; // We might need to have a seperate database table with the chores preloaded, but for now we will just use a string
    private int frequency;  // This is a number between 1 and 30 depending on the timeline
    @Enumerated(EnumType.STRING)
    private ChoreFrequencyUnitEnum choreFrequencyUnitEnum;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime lastCompletedAt;

    private LocalDateTime dueAt;

    private boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    @JsonIgnore
    private RoomEntity room;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_member_id")
    private RoomMemberEntity assignedToMember;

}
