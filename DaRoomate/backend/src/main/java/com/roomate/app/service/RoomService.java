package com.roomate.app.service;

import com.roomate.app.dto.CreateRoomRequest;
import com.roomate.app.dto.InviteUserRequest;
import com.roomate.app.dto.RoomDto;
import com.roomate.app.dto.UserDTOS.UpdateMemberRoleRequest;
import com.roomate.app.exceptions.UserApiError;

import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<RoomDto> getUserRooms(String email);
    RoomDto createRoom(CreateRoomRequest request, String headRoommateEmail) throws UserApiError;
    RoomDto joinRoom(String roomCode, String userEmail) throws UserApiError;
    RoomDto getRoomById(UUID roomId, String email) throws UserApiError;
    void removeMemberFromRoom(UUID roomId, UUID memberId, String removeremail) throws UserApiError;
    void removeRoom(UUID roomId, String email) throws UserApiError;
    void updateMemberRole(UUID roomId, UUID memberId, UpdateMemberRoleRequest request, String requestingUserId) throws UserApiError;
    void leaveRoom(UUID memberId, String email, UUID roomid) throws UserApiError;
    boolean isRoomMember(UUID roomId, String email);
    void inviteUserToRoom(InviteUserRequest request, String email) throws  UserApiError;
}
