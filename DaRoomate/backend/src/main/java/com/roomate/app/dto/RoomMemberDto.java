package com.roomate.app.dto;

import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.entities.room.RoomMemberEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Data
public class RoomMemberDto {
    private UUID id;
    private String userId;
    private String name;
    private String email;
    private RoomMemberEnum role;
    private LocalDateTime joinedAt;
    private LocalDateTime updatedAt;

    public static RoomMemberDto fromEntity(RoomMemberEntity entity) {
        RoomMemberDto dto = new RoomMemberDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId().toString() : null);
        dto.setName(entity.getUser() != null ? entity.getUser().getFirstName() + " " + entity.getUser().getLastName()
                : null);
        dto.setEmail(entity.getUser() != null ? entity.getUser().getEmail() : null);
        dto.setRole(entity.getRole());
        dto.setJoinedAt(entity.getJoinedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}