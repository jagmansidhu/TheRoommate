package com.roomate.app.dto.EventDTOS;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class EventRoomDto {
    private String roomCode;
    private String name;

    public EventRoomDto() {
    }

    public EventRoomDto(String roomCode, String name) {
        this.roomCode = roomCode;
        this.name = name;
    }
} 