package com.roomate.app.controller;

import com.roomate.app.dto.CreateRoomRequest;
import com.roomate.app.dto.InviteUserRequest;
import com.roomate.app.dto.RoomDto;
import com.roomate.app.dto.UserDTOS.UpdateMemberRoleRequest;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getUserRooms(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            List<RoomDto> rooms = roomService.getUserRooms(email);
            if (rooms.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(@RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();

            RoomDto createdRoom = roomService.createRoom(request, email);
            return ResponseEntity.ok(createdRoom);
        } catch (UserApiError e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteUserToRoom(@RequestBody InviteUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            roomService.inviteUserToRoom(request, email);
            return ResponseEntity.ok().build();
        } catch (UserApiError e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<RoomDto> joinRoom(@PathVariable String roomCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();

            RoomDto joinedRoom = roomService.joinRoom(roomCode, email);
            return ResponseEntity.ok(joinedRoom);
        } catch (UserApiError e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable UUID roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            RoomDto room = roomService.getRoomById(roomId, email);
            return ResponseEntity.ok(room);
        } catch (UserApiError e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{roomId}/members/{memberId}/role")
    public ResponseEntity<Void> updateMemberRole(@PathVariable UUID roomId, @PathVariable UUID memberId,
            @RequestBody UpdateMemberRoleRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            roomService.updateMemberRole(roomId, memberId, request, email);
            return ResponseEntity.ok().build();
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{memberid}/leave/{roomid}")
    public ResponseEntity<Void> leaveRoom(@PathVariable UUID memberid, @PathVariable UUID roomid,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            roomService.leaveRoom(memberid, email, roomid);
            return ResponseEntity.ok().build();
        } catch (UserApiError e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{roomId}/members/{memberId}")
    public ResponseEntity<Void> removeMemberFromRoom(@PathVariable UUID roomId, @PathVariable UUID memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            roomService.removeMemberFromRoom(roomId, memberId, email);
            return ResponseEntity.ok().build();
        } catch (UserApiError e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{roomId}/delete-room")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            roomService.removeRoom(roomId, email);
            return ResponseEntity.ok().build();
        } catch (UserApiError e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}