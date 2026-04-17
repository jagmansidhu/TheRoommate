package com.roomate.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(indexes = {
        @Index(name = "idx_utility_room_id", columnList = "room_id"),
        @Index(name = "idx_utility_room_member_id", columnList = "room_member_id")
})
@Data
public class UtilityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String utilityName;
    private String description;

    private double utilityPrice;

    @Enumerated(EnumType.STRING)
    private ChoreFrequencyUnitEnum choreFrequencyUnitEnum;

    @Enumerated(EnumType.STRING)
    private UtilDistributionEnum utilDistributionEnum;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime lastCompletedAt;

    private LocalDateTime dueAt;

    private boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "room_member_id")
    private RoomMemberEntity assignedToMember;


}
