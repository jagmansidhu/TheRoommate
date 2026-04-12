package com.roomate.app.service.implementation;

import com.roomate.app.dto.ChoreCreateDto;
import com.roomate.app.dto.ChoreDto;
import com.roomate.app.entities.ChoreEntity;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.repository.ChoreRepository;
import com.roomate.app.repository.RoomMemberRepository;
import com.roomate.app.repository.RoomRepository;
import com.roomate.app.repository.UserRepository;
import com.roomate.app.service.ChoreService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChoreServiceImplt implements ChoreService {
    private final RoomRepository roomRepository;
    private final ChoreRepository choreRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<ChoreDto> distributeChores(UUID roomId, ChoreCreateDto choreDTO) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        List<RoomMemberEntity> roomMembers = roomMemberRepository.findByRoomID(roomId);
        if (roomMembers.isEmpty()) {
            throw new IllegalStateException("No members in room to assign chores to");
        }

        if (choreDTO.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline is required");
        }
        LocalDateTime now = LocalDateTime.now();
        if (choreDTO.getDeadline().isAfter(now.plusYears(1))) {
            throw new IllegalArgumentException("Deadline cannot be more than one year from now");
        }
        if (choreDTO.getDeadline().isBefore(now)) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }

        List<ChoreDto> createdChores = new ArrayList<>();
        int memberIndex = 0;
        LocalDateTime dueDate = now;

        while (!dueDate.isAfter(choreDTO.getDeadline())) {
            ChoreEntity chore = new ChoreEntity();
            chore.setChoreName(choreDTO.getChoreName());
            chore.setFrequency(choreDTO.getFrequency());
            chore.setChoreFrequencyUnitEnum(choreDTO.getFrequencyUnit());
            chore.setRoom(room);
            chore.setAssignedToMember(roomMembers.get(memberIndex % roomMembers.size()));
            chore.setDueAt(dueDate);

            choreRepository.save(chore);
            createdChores.add(toDto(chore));

            memberIndex++;
            switch (choreDTO.getFrequencyUnit()) {
                case WEEKLY -> dueDate = dueDate.plusWeeks(1);
                case BIWEEKLY -> dueDate = dueDate.plusWeeks(2);
                case MONTHLY -> dueDate = dueDate.plusMonths(1);
            }
        }
        return createdChores;
    }

    @Override
    @Transactional
    public List<ChoreDto> getChoresByRoomId(UUID roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoWeeksAhead = now.plusWeeks(2);

        return choreRepository.findByRoom(room).stream().filter(chore -> chore.getDueAt() != null
                && chore.getDueAt().isAfter(now) && chore.getDueAt().isBefore(twoWeeksAhead)).map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void redistributeChores(UUID roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        List<RoomMemberEntity> roomMembers = roomMemberRepository.findByRoomID(roomId);
        List<ChoreEntity> chores = choreRepository.findByRoomWithMemberAndUser(room);

        int memberIndex = 0;
        for (ChoreEntity chore : chores) {
            chore.setAssignedToMember(roomMembers.get(memberIndex % roomMembers.size()));
            choreRepository.save(chore);
            memberIndex++;
        }
    }

    @Override
    @Transactional
    public void deleteChore(UUID choreId) {
        choreRepository.deleteById(choreId);
    }

    @Override
    @Transactional
    public void deleteChoresByType(UUID roomId, String choreName) {
        roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found"));
        choreRepository.deleteAllByRoomIdAndChoreName(roomId, choreName);
    }

    @Override
    @Transactional
    public List<ChoreDto> getChoresByUserId(String id) {
        UserEntity user = userRepository.findByEmail(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<UUID> roomMemberIds = roomMemberRepository.findAllByUserId(user.getId())
                .stream()
                .map(RoomMemberEntity::getId)
                .collect(Collectors.toList());

        return choreRepository.findAllByRoomMemberIds(roomMemberIds)
                .stream()
                .map(chore -> new ChoreDto(
                        chore.getId(),
                        chore.getChoreName(),
                        chore.getDueAt(),
                        chore.getRoom().getName()))
                .collect(Collectors.toList());
    }

    private ChoreDto toDto(ChoreEntity entity) {
        String assignedTo = (entity.getAssignedToMember() != null && entity.getAssignedToMember().getUser() != null)
                ? entity.getAssignedToMember().getUser().getEmail()
                : null;
        return new ChoreDto(entity.getId(), entity.getChoreName(), entity.getFrequency(),
                entity.getChoreFrequencyUnitEnum().name(), entity.getDueAt(), entity.isCompleted(), assignedTo,
                entity.getRoom() != null ? entity.getRoom().getId() : null);
    }
}