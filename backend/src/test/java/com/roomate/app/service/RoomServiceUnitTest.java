package com.roomate.app.service;

import com.roomate.app.dto.CreateRoomRequest;
import com.roomate.app.dto.RoomDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.entities.room.RoomMemberEnum;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.*;
import com.roomate.app.service.implementation.RoomServiceImplt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomMemberRepository roomMemberRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UtilityRepository utilityRepository;
    @Mock
    private ChoreRepository choreRepository;


    @InjectMocks
    private RoomServiceImplt roomService;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setEmail("head@example.com");
        mockUser.setFirstName("Head");
        mockUser.setLastName("User");
    }

    @Test
    void testCreateRoom_Success() throws UserApiError {
        CreateRoomRequest req = new CreateRoomRequest();
        req.setName("Test Room");
        req.setAddress("123 Test St");
        req.setDescription("Description");

        when(userRepository.getUserByEmail("head@example.com")).thenReturn(mockUser);
        when(roomRepository.countRoomsByUserId(mockUser.getId())).thenReturn(0);
        when(roomRepository.save(any(RoomEntity.class))).thenAnswer(i -> {
            RoomEntity r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        RoomDto result = roomService.createRoom(req, "head@example.com");

        assertNotNull(result);
        assertEquals("Test Room", result.getName());
        assertEquals("head@example.com", result.getHeadRoommateId());
        
        verify(roomRepository, times(1)).save(any(RoomEntity.class));
    }

    @Test
    void testCreateRoom_MaxRoomsExceeded() {
        CreateRoomRequest req = new CreateRoomRequest();
        
        when(userRepository.getUserByEmail("head@example.com")).thenReturn(mockUser);
        when(roomRepository.countRoomsByUserId(mockUser.getId())).thenReturn(3);

        UserApiError ex = assertThrows(UserApiError.class, () -> roomService.createRoom(req, "head@example.com"));
        assertEquals("User has reached the maximum number of rooms (3).", ex.getMessage());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void testJoinRoom_Success() throws UserApiError {
        String joinCode = "JOIN123";
        
        UserEntity joiner = new UserEntity();
        joiner.setId(2L);
        joiner.setEmail("joiner@example.com");

        RoomEntity room = new RoomEntity("Test Room", "Address", "Desc", joinCode, mockUser.getEmail(), new ArrayList<>());
        room.setId(UUID.randomUUID());
        
        // Add existing member
        room.getMembers().add(new RoomMemberEntity(room, mockUser, RoomMemberEnum.HEAD_ROOMMATE));

        when(userRepository.getUserByEmail("joiner@example.com")).thenReturn(joiner);
        when(roomRepository.countRoomsByUserId(joiner.getId())).thenReturn(0);
        when(roomRepository.findByRoomCode(joinCode)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(RoomEntity.class))).thenAnswer(i -> i.getArgument(0));

        RoomDto result = roomService.joinRoom(joinCode, "joiner@example.com");

        assertNotNull(result);
        assertEquals(2, result.getMembers().size()); // 1 original + 1 joiner
        verify(roomRepository, times(1)).save(room);
    }
}
