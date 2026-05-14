package com.roomate.app.service;

import com.roomate.app.dto.CreateRoomRequest;
import com.roomate.app.dto.RoomDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEnum;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.RoomRepository;
import com.roomate.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomServiceIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void testCreateRoomAndCascadeSaveMembers() throws UserApiError {
        // Arrange
        UserEntity headRoommate = new UserEntity();
        headRoommate.setEmail("head.roommate@example.com");
        headRoommate.setFirstName("Boss");
        headRoommate.setLastName("Man");
        headRoommate.setPassword("EncodedPass");
        userRepository.save(headRoommate);

        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("The Test Cave");
        request.setAddress("404 Not Found Ave");
        request.setDescription("A very dark testing cave");

        // Act
        RoomDto createdRoom = roomService.createRoom(request, headRoommate.getEmail());

        // Assert DTO return
        assertNotNull(createdRoom);
        assertNotNull(createdRoom.getId());
        assertEquals("The Test Cave", createdRoom.getName());
        assertEquals(headRoommate.getEmail(), createdRoom.getHeadRoommateId());
        
        // Ensure members array is mapped properly in DTO
        assertNotNull(createdRoom.getMembers());
        assertEquals(1, createdRoom.getMembers().size());
        assertEquals(RoomMemberEnum.HEAD_ROOMMATE, createdRoom.getMembers().get(0).getRole());

        // Assert DB Persistence mapping
        RoomEntity savedRoom = roomRepository.findById(createdRoom.getId()).orElse(null);
        assertNotNull(savedRoom);
        assertEquals(1, savedRoom.getMembers().size());
        assertNotNull(savedRoom.getRoomCode());
    }

    @Test
    void testJoinRoomAddsMember() throws UserApiError {
        // Arrange - Create Head Roommate and Room
        UserEntity head = new UserEntity();
        head.setEmail("head@example.com");
        head.setPassword("pass");
        userRepository.save(head);

        CreateRoomRequest joinerReq = new CreateRoomRequest();
        joinerReq.setName("House");
        joinerReq.setAddress("123 St");
        joinerReq.setDescription("");
        RoomDto roomDto = roomService.createRoom(joinerReq, head.getEmail());
        
        // Arrange - Create Joiner
        UserEntity joiner = new UserEntity();
        joiner.setEmail("joiner@example.com");
        joiner.setPassword("pass");
        userRepository.save(joiner);

        // Act
        RoomDto updatedRoom = roomService.joinRoom(roomDto.getRoomCode(), joiner.getEmail());

        // Assert
        assertEquals(2, updatedRoom.getMembers().size());
        
        // Assure both users pull rooms correctly
        List<RoomDto> headRooms = roomService.getUserRooms(head.getEmail());
        List<RoomDto> joinerRooms = roomService.getUserRooms(joiner.getEmail());
        
        assertEquals(1, headRooms.size());
        assertEquals(1, joinerRooms.size());
        assertEquals(headRooms.get(0).getId(), joinerRooms.get(0).getId());
    }
}
