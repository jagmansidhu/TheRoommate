package com.roomate.app.service.implementation;

import com.roomate.app.dto.UtilityCreateDto;
import com.roomate.app.dto.UtilityDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.entities.UtilityEntity;
import com.roomate.app.entities.UtilDistributionEnum;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.repository.RoomMemberRepository;
import com.roomate.app.repository.RoomRepository;
import com.roomate.app.repository.UserRepository;
import com.roomate.app.repository.UtilityRepository;
import com.roomate.app.service.UtilityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UtilityServiceImplt implements UtilityService {

    private final UtilityRepository utilityRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<UtilityEntity> createUtility(UtilityCreateDto dto) {
        RoomEntity room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        Hibernate.initialize(room.getMembers());

        List<UtilityEntity> createdUtilities = new ArrayList<>();

        LocalDateTime startingDate = dto.getStartingDate() != null ? dto.getStartingDate() : LocalDateTime.now();
        LocalDateTime deadline = dto.getDeadline();
        boolean recurring = (dto.getFrequencyUnit() != null) && (deadline != null) && !startingDate.isAfter(deadline);

        LocalDateTime dueDate = startingDate;

        do {
            final LocalDateTime currentDueDate = dueDate;

            if (dto.getUtilDistributionEnum() == UtilDistributionEnum.EQUALSPLIT) {
                List<RoomMemberEntity> members = room.getMembers();
                double share = dto.getUtilityPrice() / members.size();

                for (RoomMemberEntity member : members) {
                    UtilityEntity utility = new UtilityEntity();
                    utility.setUtilityName(dto.getUtilityName());
                    utility.setDescription(dto.getDescription());
                    utility.setUtilityPrice(share);
                    utility.setUtilDistributionEnum(dto.getUtilDistributionEnum());
                    utility.setRoom(room);
                    utility.setAssignedToMember(member);
                    utility.setDueAt(currentDueDate);
                    utility.setChoreFrequencyUnitEnum(dto.getFrequencyUnit());
                    createdUtilities.add(utilityRepository.save(utility));
                }
            } else if (dto.getUtilDistributionEnum() == UtilDistributionEnum.CUSTOMSPLIT) {
                dto.getCustomSplit().forEach((memberId, splitAmount) -> {
                    RoomMemberEntity member = roomMemberRepository.findById(memberId)
                            .orElseThrow(() -> new EntityNotFoundException("Member not found"));

                    UtilityEntity utility = new UtilityEntity();
                    utility.setUtilityName(dto.getUtilityName());
                    utility.setDescription(dto.getDescription());
                    utility.setUtilityPrice(splitAmount);
                    utility.setUtilDistributionEnum(dto.getUtilDistributionEnum());
                    utility.setRoom(room);
                    utility.setAssignedToMember(member);
                    utility.setDueAt(currentDueDate);
                    utility.setChoreFrequencyUnitEnum(dto.getFrequencyUnit());
                    createdUtilities.add(utilityRepository.save(utility));
                });
            }

            if (!recurring) break;

            if (dto.getFrequencyUnit() != null) {
                switch (dto.getFrequencyUnit()) {
                    case WEEKLY -> dueDate = dueDate.plusWeeks(1);
                    case BIWEEKLY -> dueDate = dueDate.plusWeeks(2);
                    case MONTHLY -> dueDate = dueDate.plusMonths(1);
                    default -> dueDate = dueDate.plusMonths(1);
                }
            } else {
                break;
            }
        } while (!dueDate.isAfter(deadline));

        return createdUtilities;
    }

    @Override
    @Transactional
    public List<UtilityDto> getUtilitiesByRoom(UUID roomId) {
        roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found"));

        return utilityRepository
                .findByRoomId(roomId).stream().map(utility -> new UtilityDto(utility.getId(), utility.getUtilityName(),
                        utility.getUtilityPrice(), utility.getRoom() != null ? utility.getRoom().getId() : null,
                        utility.isCompleted()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UtilityDto> getUtilitiesByRoomandMemberId(UUID roomId, UUID memberId) {
        roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found"));

        return utilityRepository.findByRoomIdAndMemberId(roomId, memberId).stream()
                .map(utility -> new UtilityDto(utility.getId(), utility.getUtilityName(), utility.getUtilityPrice(),
                        utility.getRoom() != null ? utility.getRoom().getId() : null, utility.isCompleted()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<UtilityDto> getUpcomingUtilities(String id) {
        UserEntity user = userRepository.findByEmail(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<UUID> roomMemberIds = roomMemberRepository.findAllByUserId(user.getId())
                .stream()
                .map(RoomMemberEntity::getId)
                .collect(Collectors.toList());

        return utilityRepository.findAllByRoomMemberIds(roomMemberIds)
                .stream()
                .map(utility -> new UtilityDto(
                        utility.getId(),
                        utility.getUtilityName(),
                        utility.getUtilityPrice(),
                        utility.getRoom() != null ? utility.getRoom().getName() : null,
                        utility.getDueAt(),
                        utility.isCompleted()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUtility(UUID utilityId) {
        if (!utilityRepository.existsById(utilityId)) {
            throw new EntityNotFoundException("Utility with id " + utilityId + " not found");
        }
        utilityRepository.deleteById(utilityId);
    }

    @Override
    @Transactional
    public UtilityDto updateCompletion(UUID utilityId, String userEmail, boolean completed) {
        UtilityEntity utility = utilityRepository.findByUtilityId(utilityId)
                .orElseThrow(() -> new EntityNotFoundException("Utility not found"));

        if (utility.getAssignedToMember() == null || utility.getAssignedToMember().getUser() == null) {
            throw new IllegalStateException("Utility is not assigned to a member");
        }

        String assigneeEmail = utility.getAssignedToMember().getUser().getEmail();
        if (!assigneeEmail.equalsIgnoreCase(userEmail)) {
            throw new SecurityException("Only the assigned roommate can update completion");
        }

        utility.setCompleted(completed);
        utility.setLastCompletedAt(completed ? LocalDateTime.now() : null);

        UtilityEntity updated = utilityRepository.save(utility);
        return new UtilityDto(updated);
    }



}
