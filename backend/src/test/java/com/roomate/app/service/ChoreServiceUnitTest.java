package com.roomate.app.service;

import com.roomate.app.entities.ChoreEntity;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.ChoreRepository;
import com.roomate.app.repository.RoomMemberRepository;
import com.roomate.app.repository.RoomRepository;
import com.roomate.app.service.implementation.ChoreServiceImplt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChoreServiceUnitTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ChoreRepository choreRepository;
    @Mock
    private RoomMemberRepository roomMemberRepository;

    @InjectMocks
    private ChoreServiceImplt choreService;

    @Test
    void nonMemberCannotDeleteChore() {
        UUID choreId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        RoomEntity room = new RoomEntity();
        room.setId(roomId);
        ChoreEntity chore = new ChoreEntity();
        chore.setId(choreId);
        chore.setRoom(room);

        when(choreRepository.findByChoreId(choreId)).thenReturn(Optional.of(chore));
        when(roomMemberRepository.findByRoomIdAndUserEmail(roomId, "outsider@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserApiError.class,
                () -> choreService.deleteChore(choreId, "outsider@example.com"));
        verify(choreRepository, never()).deleteById(choreId);
    }
}
