package com.roomate.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Data
public class RoomDto {
    private UUID id;
    private String name;
    private String address;
    private String description;
    private String roomCode;
    private String headRoommateId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RoomMemberDto> members;

    public RoomDto(@NotNull String roomCode) {
        this.roomCode = roomCode;
    }

    public RoomDto() {

    }
}


