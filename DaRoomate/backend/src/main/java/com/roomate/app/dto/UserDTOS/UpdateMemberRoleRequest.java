package com.roomate.app.dto.UserDTOS;

import com.roomate.app.entities.room.RoomMemberEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRoleRequest {
    private RoomMemberEnum role;
}
