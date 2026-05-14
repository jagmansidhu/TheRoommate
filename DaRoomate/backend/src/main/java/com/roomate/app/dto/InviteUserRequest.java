package com.roomate.app.dto;

import lombok.Data;

@Data
public class InviteUserRequest {
    public String email;
    public String roomId;
}
