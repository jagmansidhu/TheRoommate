package com.roomate.app.dto;

import com.roomate.app.entities.UtilityEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UtilityDto {
    private UUID id;
    private String utilityName;
    private Double utilityPrice;
    private UUID roomId;
    private UUID roomMemberId;
    private String roomName;
    private LocalDateTime dueDate;
    private LocalDateTime dueAt;
    private boolean isCompleted;

    public UtilityDto(UtilityEntity entity) {
        this.id = entity.getId();
        this.utilityName = entity.getUtilityName();
        this.utilityPrice = entity.getUtilityPrice();
        this.roomId = entity.getRoom() != null ? entity.getRoom().getId() : null;
        this.isCompleted = entity.isCompleted();
        this.dueAt = entity.getDueAt();
        this.dueDate = entity.getDueAt();
    }
    public UtilityDto(UUID id, String utilityName, Double utilityPrice, UUID roomId, boolean isCompleted) {
        this.id = id;
        this.utilityName = utilityName;
        this.utilityPrice = utilityPrice;
        this.roomId = roomId;
        this.isCompleted = isCompleted;
    }

     public UtilityDto(UUID id, String utilityName, Double utilityPrice, String roomNate, LocalDateTime dueDate, boolean isCompleted) {
        this.id = id;
        this.utilityName = utilityName;
        this.utilityPrice = utilityPrice;
        this.roomName = roomNate;
        this.dueDate = dueDate;
        this.dueAt = dueDate;
        this.isCompleted = isCompleted;
    }

}
