package com.roomate.app.service;

import com.roomate.app.dto.ledger.AssignSplitsDto;
import com.roomate.app.entities.ledger.LedgerEntryEntity;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.entities.room.RoomMemberEnum;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.LedgerEntryRepository;
import com.roomate.app.repository.LedgerSplitRepository;
import com.roomate.app.repository.RoomMemberRepository;
import com.roomate.app.repository.RoomRepository;
import com.roomate.app.service.implementation.LedgerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerServiceUnitTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private LedgerSplitRepository ledgerSplitRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomMemberRepository roomMemberRepository;

    @InjectMocks
    private LedgerServiceImpl ledgerService;

    @Test
    void splitMemberMustBeInTheRoom() {
        UUID entryId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID otherRoomId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        String headEmail = "head@example.com";

        RoomEntity room = new RoomEntity();
        room.setId(roomId);
        RoomEntity otherRoom = new RoomEntity();
        otherRoom.setId(otherRoomId);

        LedgerEntryEntity entry = new LedgerEntryEntity();
        entry.setRoom(room);

        RoomMemberEntity head = new RoomMemberEntity();
        head.setRole(RoomMemberEnum.HEAD_ROOMMATE);

        RoomMemberEntity outsider = new RoomMemberEntity();
        outsider.setId(memberId);
        outsider.setRoom(otherRoom);

        when(ledgerEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));
        when(roomMemberRepository.findByRoomIdAndUserEmail(roomId, headEmail)).thenReturn(Optional.of(head));
        when(roomMemberRepository.findById(memberId)).thenReturn(Optional.of(outsider));

        AssignSplitsDto.SplitAssignment assignment = new AssignSplitsDto.SplitAssignment();
        assignment.setMemberId(memberId);
        assignment.setAmount(new BigDecimal("10.00"));
        AssignSplitsDto dto = new AssignSplitsDto(List.of(assignment));

        assertThrows(UserApiError.class, () -> ledgerService.assignSplits(entryId, dto, headEmail));
        verify(ledgerEntryRepository, never()).save(any());
    }
}
