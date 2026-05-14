package com.roomate.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChoreDto {
    public UUID id;
    public String choreName;
    public int frequency;
    public String frequencyUnit;
    public LocalDateTime dueAt;
    public boolean isCompleted;
    public String assignedToMemberName;
    public UUID roomid;
    public String roomName;

    public ChoreDto(UUID id, String choreName, int frequency, String frequencyUnit, LocalDateTime dueAt, boolean isCompleted, String assignedToMemberName) {
        this.id = id;
        this.choreName = choreName;
        this.frequency = frequency;
        this.frequencyUnit = frequencyUnit;
        this.dueAt = dueAt;
        this.isCompleted = isCompleted;
        this.assignedToMemberName = assignedToMemberName;
    }

    public ChoreDto(UUID id, String choreName, int frequency, String frequencyUnit, LocalDateTime dueAt, boolean isCompleted, String assignedToMemberName, UUID roomid) {
        this.id = id;
        this.choreName = choreName;
        this.frequency = frequency;
        this.frequencyUnit = frequencyUnit;
        this.dueAt = dueAt;
        this.isCompleted = isCompleted;
        this.assignedToMemberName = assignedToMemberName;
        this.roomid = roomid;
    }

    public ChoreDto(UUID id, String choreName, LocalDateTime dueAt, String roomName, boolean isCompleted) {
        this.id = id;
        this.choreName = choreName;
        this.dueAt = dueAt;
        this.roomName = roomName;
        this.isCompleted = isCompleted;

    }
}

