package com.roomate.app.service.implementation;

import com.roomate.app.dto.UtilityCreateDto;
import com.roomate.app.dto.UtilityDto;
import com.roomate.app.entities.UtilityEntity;
import com.roomate.app.entities.UtilDistributionEnum;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.RoomMemberRepository;
import com.roomate.app.repository.RoomRepository;
import com.roomate.app.repository.UtilityRepository;
import com.roomate.app.service.UtilityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilityServiceImplt implements UtilityService {

    private final UtilityRepository utilityRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"roomUtilities", "userUtilities"}, allEntries = true)
    public List<UtilityDto> createUtility(UtilityCreateDto dto, String userEmail) {
        RoomEntity room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        roomMemberRepository.findByRoomIdAndUserEmail(room.getId(), userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));
        Hibernate.initialize(room.getMembers());



        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startingDate = dto.getStartingDate() != null ? dto.getStartingDate() : now;
        LocalDateTime deadline = dto.getDeadline();
        boolean recurring = dto.getFrequencyUnit() != null;

        if (startingDate.toLocalDate().isBefore(now.toLocalDate().minusDays(1))) {
            throw new IllegalArgumentException("Starting date cannot be in the past");
        }

        if (recurring) {
            if (deadline == null) {
                throw new IllegalArgumentException("Deadline is required for recurring utilities");
            }
            if (deadline.isBefore(now.minusDays(1))) {
                throw new IllegalArgumentException("Deadline must be in the future");
            }
            if (deadline.isAfter(now.plusYears(1).plusDays(1))) {
                throw new IllegalArgumentException("Deadline cannot be more than one year from now");
            }
            if (startingDate.isAfter(deadline)) {
                throw new IllegalArgumentException("Deadline must be after starting date");
            }
        }

        Map<UUID, RoomMemberEntity> customSplitMemberMap = Map.of();
        if (dto.getUtilDistributionEnum() == UtilDistributionEnum.CUSTOMSPLIT) {
            if (dto.getCustomSplit() == null || dto.getCustomSplit().isEmpty()) {
                throw new IllegalArgumentException("Custom split is required when distribution is CUSTOMSPLIT");
            }

            List<RoomMemberEntity> members = roomMemberRepository.findAllById(dto.getCustomSplit().keySet());
            customSplitMemberMap = members.stream()
                    .collect(Collectors.toMap(RoomMemberEntity::getId, member -> member));

            if (customSplitMemberMap.size() != dto.getCustomSplit().size()) {
                throw new EntityNotFoundException("One or more room members were not found");
            }
        }

        List<UtilityEntity> toSave = new ArrayList<>();
        LocalDateTime dueDate = startingDate;

        do {
            final LocalDateTime currentDueDate = dueDate;

            if (dto.getUtilDistributionEnum() == UtilDistributionEnum.EQUALSPLIT) {
                List<RoomMemberEntity> members = room.getMembers();
                int count = members.size();
                BigDecimal price = BigDecimal.valueOf(dto.getUtilityPrice());
                BigDecimal share = price.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                BigDecimal remainder = price.subtract(share.multiply(BigDecimal.valueOf(count)));

                for (int i = 0; i < count; i++) {
                    BigDecimal amount = share;
                    if (i == 0 && remainder.compareTo(BigDecimal.ZERO) != 0) {
                        amount = amount.add(remainder);
                    }
                    RoomMemberEntity member = members.get(i);
                    UtilityEntity utility = new UtilityEntity();
                    utility.setUtilityName(dto.getUtilityName());
                    utility.setDescription(dto.getDescription());
                    utility.setUtilityPrice(amount.doubleValue()); // shortcut: price column is double, upgrade when the column is BigDecimal
                    utility.setUtilDistributionEnum(dto.getUtilDistributionEnum());
                    utility.setRoom(room);
                    utility.setAssignedToMember(member);
                    utility.setDueAt(currentDueDate);
                    utility.setChoreFrequencyUnitEnum(dto.getFrequencyUnit());
                    toSave.add(utility);
                }
            } else if (dto.getUtilDistributionEnum() == UtilDistributionEnum.CUSTOMSPLIT) {
                for (Map.Entry<UUID, Double> entry : dto.getCustomSplit().entrySet()) {
                    RoomMemberEntity member = customSplitMemberMap.get(entry.getKey());
                    Double splitAmount = entry.getValue();
                    if (member == null) {
                        throw new EntityNotFoundException("Member not found");
                    }
                    if (!member.getRoom().getId().equals(room.getId())) {
                        throw new UserApiError("Custom split member is not in this room");
                    }

                    UtilityEntity utility = new UtilityEntity();
                    utility.setUtilityName(dto.getUtilityName());
                    utility.setDescription(dto.getDescription());
                    utility.setUtilityPrice(splitAmount);
                    utility.setUtilDistributionEnum(dto.getUtilDistributionEnum());
                    utility.setRoom(room);
                    utility.setAssignedToMember(member);
                    utility.setDueAt(currentDueDate);
                    utility.setChoreFrequencyUnitEnum(dto.getFrequencyUnit());
                    toSave.add(utility);
                }
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

        return utilityRepository.saveAll(toSave).stream()
                .map(utility -> new UtilityDto(utility.getId(), utility.getUtilityName(),
                        utility.getUtilityPrice(),
                        utility.getRoom() != null ? utility.getRoom().getId() : null,
                        utility.getDueAt(),
                        utility.getChoreFrequencyUnitEnum(),
                        utility.isCompleted()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Cacheable(value = "roomUtilities", key = "#roomId")
    public List<UtilityDto> getUtilitiesByRoom(UUID roomId) {
        return utilityRepository
                .findByRoomId(roomId).stream().map(utility -> new UtilityDto(utility.getId(), utility.getUtilityName(),
                        utility.getUtilityPrice(),
                        utility.getRoom() != null ? utility.getRoom().getId() : null,
                        utility.getDueAt(),
                        utility.getChoreFrequencyUnitEnum(),
                        utility.isCompleted()))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "roomUtilities", key = "#roomId + '-' + #memberId")
    public List<UtilityDto> getUtilitiesByRoomandMemberId(UUID roomId, UUID memberId) {
        return utilityRepository.findByRoomIdAndMemberId(roomId, memberId).stream()
                .map(utility -> new UtilityDto(utility.getId(), utility.getUtilityName(), utility.getUtilityPrice(),
                        utility.getRoom() != null ? utility.getRoom().getId() : null,
                        utility.getDueAt(),
                        utility.getChoreFrequencyUnitEnum(),
                        utility.isCompleted()))
                .collect(Collectors.toList());
    }

    @Override
    public void validateRoomMembership(UUID roomId, String userEmail) {
        roomMemberRepository.findByRoomIdAndUserEmail(roomId, userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));
    }

    @Override
    @Transactional
    @Cacheable(value = "userUtilities", key = "#id")
    public List<UtilityDto> getUpcomingUtilities(String id) {
        return utilityRepository.findAllByUserEmail(id)
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
    @Transactional
    @CacheEvict(value = {"roomUtilities", "userUtilities"}, allEntries = true)
    public void deleteUtility(UUID utilityId, String userEmail) {
        UtilityEntity utility = utilityRepository.findByUtilityId(utilityId)
                .orElseThrow(() -> new EntityNotFoundException("Utility with id " + utilityId + " not found"));
        roomMemberRepository.findByRoomIdAndUserEmail(utility.getRoom().getId(), userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));
        utilityRepository.deleteById(utilityId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"roomUtilities", "userUtilities"}, allEntries = true)
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
