package com.roomate.app.dto.EventDTOS;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class EventUserDto {
    private String firstName;
    private String lastName;
    private String email;

    public EventUserDto() {
    }

    public EventUserDto(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
} 