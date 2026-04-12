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
                createdUtilities.add(utilityRepository.save(utility));
            }
        } else if (dto.getUtilDistributionEnum() == UtilDistributionEnum.CUSTOMSPLIT) {
            dto.getCustomSplit().forEach((memberId, percentage) -> {
                RoomMemberEntity member = roomMemberRepository.findById(memberId)
                        .orElseThrow(() -> new EntityNotFoundException("Member not found"));
                double shareAmount = (percentage / 100.0) * dto.getUtilityPrice();

                UtilityEntity utility = new UtilityEntity();
                utility.setUtilityName(dto.getUtilityName());
                utility.setDescription(dto.getDescription());
                utility.setUtilityPrice(shareAmount);
                utility.setUtilDistributionEnum(dto.getUtilDistributionEnum());
                utility.setRoom(room);
                utility.setAssignedToMember(member);
                createdUtilities.add(utilityRepository.save(utility));
            });
        }

        return createdUtilities;
    }

    @Override
    @Transactional
    public List<UtilityDto> getUtilitiesByRoom(UUID roomId) {
        roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found"));

        return utilityRepository
                .findByRoomId(roomId).stream().map(utility -> new UtilityDto(utility.getId(), utility.getUtilityName(),
                        utility.getUtilityPrice(), utility.getRoom() != null ? utility.getRoom().getId() : null))
                .collect(Collectors.toList());
    }

    @Override
    public List<UtilityDto> getUtilitiesByRoomandMemberId(UUID roomId, UUID memberId) {
        roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found"));

        return utilityRepository.findByRoomIdAndMemberId(roomId, memberId).stream()
                .map(utility -> new UtilityDto(utility.getId(), utility.getUtilityName(), utility.getUtilityPrice(),
                        utility.getRoom() != null ? utility.getRoom().getId() : null))
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
                        utility.getDueAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUtility(UUID utilityId) {
        if (!utilityRepository.existsById(utilityId)) {
            throw new EntityNotFoundException("Utility with id " + utilityId + " not found");
        }
        utilityRepository.deleteById(utilityId);
    }

    // TODO handle OVerall Logic for updating utilities when users are added or
    // removed from a room
    @Override
    @Transactional
    public void updateUtilitiesOnUserChange(UUID roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        Hibernate.initialize(room.getMembers());

        List<RoomMemberEntity> members = room.getMembers();
        if (members.isEmpty()) {
            throw new IllegalStateException("Room has no members.");
        }

        List<UtilityEntity> utilities = utilityRepository.findByRoomId(roomId);

        for (UtilityEntity utility : utilities) {
            if (utility.getUtilDistributionEnum() == UtilDistributionEnum.EQUALSPLIT) {
                double share = utility.getUtilityPrice() / members.size();
                for (RoomMemberEntity member : members) {
                    utility.setAssignedToMember(member);
                    utility.setUtilityPrice(share);
                    utilityRepository.save(utility);
                }
            } else if (utility.getUtilDistributionEnum() == UtilDistributionEnum.CUSTOMSPLIT) {
                throw new UnsupportedOperationException("Custom split logic needs to be implemented.");
            }
        }
    }

}
