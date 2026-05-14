package com.roomate.app.entities;

import com.roomate.app.entities.room.RoomEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToOne
    private RoomEntity room;

    @ManyToOne
    private UserEntity user;

    private LocalDateTime created;
    private LocalDateTime updated;
}
