package com.roomate.app.service.implementation;

import com.roomate.app.dto.UserDTOS.UpdateMemberRoleRequest;
import com.roomate.app.mailer.RoomInviteMailSender;
import com.roomate.app.dto.*;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.entities.room.RoomMemberEnum;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.*;
import com.roomate.app.service.RoomService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RoomServiceImplt implements RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImplt.class);

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final EventRepository eventRepository;
    private final UtilityRepository utilityRepository;
    private final ChoreRepository choreRepository;
    private final LedgerSplitRepository ledgerSplitRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final GroceryListRepository groceryListRepository;
    private final GroceryItemRepository groceryItemRepository;
    @Autowired
    private RoomInviteMailSender mailSender;

    public RoomServiceImplt(UserRepository userRepository, RoomRepository roomRepository,
            RoomMemberRepository roomMemberRepository,
            EventRepository eventRepository,
            UtilityRepository utilityRepository,
            ChoreRepository choreRepository,
            LedgerSplitRepository ledgerSplitRepository,
            LedgerEntryRepository ledgerEntryRepository,
            GroceryListRepository groceryListRepository,
            GroceryItemRepository groceryItemRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.eventRepository = eventRepository;
        this.utilityRepository = utilityRepository;
        this.choreRepository = choreRepository;
        this.ledgerSplitRepository = ledgerSplitRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.groceryListRepository = groceryListRepository;
        this.groceryItemRepository = groceryItemRepository;
    }

    @Override
    @Transactional
    public List<RoomDto> getUserRooms(String email) {
        UserEntity user = userRepository.getUserByEmail(email);

        if (user == null) {
            return List.of();
        }

        List<RoomEntity> roomEntities = roomRepository.findByMemberUserId(user.getId());

        return roomEntities.stream().map(this::convertToRoomDto).toList();
    }

    @Override
    @Transactional
    public RoomDto createRoom(CreateRoomRequest request, String headRoomateEmail) throws UserApiError {
        UserEntity user = userRepository.getUserByEmail(headRoomateEmail);
        if (user == null) {
            throw new UserApiError("Head roommate user not found with ID: " + headRoomateEmail);
        }

        userRepository.lockUserRow(user.getId());

        if (roomRepository.countRoomsByUserId(user.getId()) >= 3) {
            throw new UserApiError("User has reached the maximum number of rooms (3).");
        }
        String roomCode = generateUniqueRoomCode();

        RoomEntity room = new RoomEntity(request.getName(), request.getAddress(), request.getDescription(), roomCode,
                headRoomateEmail, new ArrayList<>());

        RoomMemberEntity roomMemberEntity = new RoomMemberEntity(room, user, RoomMemberEnum.HEAD_ROOMMATE);

        room.getMembers().add(roomMemberEntity);

        RoomEntity savedRoom;
        try {
            savedRoom = roomRepository.save(room);
            logger.info("Saved room and its members: {}", savedRoom);
        } catch (Exception e) {
            logger.error("Error saving room or its members: {}", e.getMessage(), e);
            throw new UserApiError("Failed to create room: " + e.getMessage());
        }

        return convertToRoomDto(savedRoom);
    }

    @Override
    @Transactional
    public RoomDto joinRoom(String roomCode, String email) throws UserApiError {
        UserEntity user = userRepository.getUserByEmail(email);
        if (user == null) {
            throw new UserApiError("User not found with ID: " + email);
        }

        userRepository.lockUserRow(user.getId());

        if (roomRepository.countRoomsByUserId(user.getId()) >= 3) {
            throw new UserApiError("User has reached the maximum number of rooms (3).");
        }

        if (roomRepository.lockByRoomCode(roomCode) == null) {
            throw new UserApiError("Room not found with code: " + roomCode);
        }
        RoomEntity room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new UserApiError("Room not found with code: " + roomCode));

        if (room.getMembers().size() >= 6) {
            throw new UserApiError("Room is full. Maximum of 6 members allowed.");
        }

        boolean alreadyMember = room.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()));

        if (alreadyMember) {
            throw new UserApiError("User is already a member of this room.");
        }

        RoomMemberEntity member = new RoomMemberEntity(room, user, RoomMemberEnum.ROOMMATE);

        room.getMembers().add(member);

        return convertToRoomDto(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomDto getRoomById(UUID roomId, String email) throws UserApiError {
        RoomEntity room = roomRepository.getRoomEntityById(roomId)
                .orElseThrow(() -> new UserApiError("Room not found with ID: " + roomId));

        UserEntity requestingUser = userRepository.getUserByEmail(email);

        if (requestingUser == null || !isRoomMember(roomId, email)) {
            throw new UserApiError("User is not authorized to view this room.");
        }

        return convertToRoomDto(room);
    }

    @Override
    @Transactional
    public void removeMemberFromRoom(UUID roomId, UUID memberId, String removerAuthId) throws UserApiError {
        RoomMemberEntity member = roomMemberRepository.getRoomMemberEntityById(memberId)
                .orElseThrow(() -> new UserApiError("Room member not found with ID: " + memberId));

        RoomEntity room = roomRepository.getRoomEntityById(roomId)
                .orElseThrow(() -> new UserApiError("Room not found with ID: " + roomId));

        System.out.println("Removing member " + member.getUser().getId() + " from room " + room.getId());

        boolean isRemoverHead = room.getHeadRoommateId().equals(removerAuthId);
        boolean isSelfRemove = member.getUser().getEmail().equals(removerAuthId);

        if (!isRemoverHead || isSelfRemove) {
            throw new UserApiError("Not authorized to remove this member.");
        }

        clearMemberReferences(member.getId());
        roomMemberRepository.deleteByRoomIdAndUserId(roomId, member.getUser().getId());
    }

    @Override
    @Transactional
    public void leaveRoom(UUID memberid, String email, UUID roomid) throws UserApiError {
        UserEntity user = userRepository.getUserByEmail(email);

        RoomMemberEntity member = roomMemberRepository.getRoomMemberEntityById(memberid)
                .orElseThrow(() -> new UserApiError("Room member not found with ID: " + user.getId()));

        if (member.getRole() == RoomMemberEnum.HEAD_ROOMMATE) {
            throw new UserApiError("Cannot have the head roommate leave the room.");
        }

        if (!member.getUser().getId().equals(user.getId())) {
            throw new UserApiError("Not authorized to remove this member.");
        }
        if (!member.getRoom().getId().equals(roomid)) {
            throw new UserApiError("Member does not belong to the specified room.");
        }

        clearMemberReferences(member.getId());
        roomMemberRepository.deleteByMemberIdAndUserId(memberid, user.getId());

    }

    @Override
    @Transactional
    public void removeRoom(UUID roomId, String requesterEmail) throws UserApiError {
        UserEntity user = userRepository.getUserByEmail(requesterEmail);
        if (user == null) {
            throw new UserApiError("User not found with ID: " + requesterEmail);
        }

        RoomEntity room = roomRepository.getRoomEntityById(roomId)
                .orElseThrow(() -> new UserApiError("Room not found with ID: " + roomId));

        if (!room.getHeadRoommateId().equals(requesterEmail)) {
            throw new UserApiError("Not authorized to delete room.");
        }

        // CascadeType.ALL + orphanRemoval on RoomEntity handles all child deletions
        // (chores, utilities, events, grocery lists, ledger entries, members) automatically.
        roomRepository.deleteById(roomId);
        logger.info("Room {} deleted by {} — all child data cascade-deleted", roomId, requesterEmail);
    }


    @Override
    @Transactional
    public void updateMemberRole(UUID roomId, UUID memberId, UpdateMemberRoleRequest request,
            String requestingUserEmail) throws UserApiError {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new UserApiError("Room not found with ID: " + roomId));

        UserEntity requestingUser = userRepository.getUserByEmail(requestingUserEmail);
        if (requestingUser == null) {
            throw new UserApiError("User Not Found with ID: " + requestingUserEmail);
        }

        if (!requestingUserEmail.equals(room.getHeadRoommateId())) {
            throw new UserApiError("Only the head roommate can change member roles.");
        }

        if (request.getRole() == RoomMemberEnum.HEAD_ROOMMATE) {
            throw new UserApiError("Cannot assign the head roommate role.");
        }

        RoomMemberEntity member = roomMemberRepository.findById(memberId)
                .orElseThrow(() -> new UserApiError("Member not found with ID: " + memberId));

        if (!member.getRoom().getId().equals(roomId)) {
            throw new UserApiError("Member does not belong to the specified room.");
        }

        if (member.getRole() == RoomMemberEnum.HEAD_ROOMMATE) {
            throw new UserApiError(
                    "Cannot change the head roommate's role directly. Transfer head roommate status first.");
        }

        member.setRole(request.getRole());
        member.setUpdatedAt(LocalDateTime.now());

        roomMemberRepository.save(member);
    }

    @Override
    public boolean isRoomMember(UUID roomId, String email) {
        UserEntity user = userRepository.getUserByEmail(email);
        if (user == null) {
            return false;
        }
        return roomMemberRepository.findByRoomIdAndUserId(roomId, user.getId()).isPresent();
    }

    @Override
    public void inviteUserToRoom(InviteUserRequest request, String email) throws UserApiError {
        if (request.getRoomId() == null) {
            throw new UserApiError("Room ID cannot be null.");
        }
        RoomEntity room = roomRepository.findById(UUID.fromString(request.getRoomId()))
                .orElseThrow(() -> new UserApiError("Room not found with ID: " + request.getRoomId()));
        roomMemberRepository.findByRoomIdAndUserEmail(room.getId(), email)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

        if (!userRepository.existsByEmail(request.getEmail())) {
            mailSender.sendMail(request.email, "Room Invitation",
                    "You have been invited to join the room! However, you have not created an account. Please create and account first and then join this room! : "
                            + room.getRoomCode() +
                            ". Please use this code to join the room in the app.");
        } else {
            mailSender.sendMail(request.email, "Room Invitation",
                    "You have been invited to join the room: " + room.getRoomCode() +
                            ". Please use this code to join the room in the app.");
        }
    }

    private void clearMemberReferences(UUID memberId) {
        ledgerSplitRepository.deleteAll(ledgerSplitRepository.findByRoomMemberId(memberId));
        utilityRepository.deleteAllByRoomMemberId(memberId);
        choreRepository.deleteByAssignedMemberId(memberId);
        ledgerEntryRepository.clearCreatedByMember(memberId);
        groceryListRepository.clearCreatedByMember(memberId);
        groceryItemRepository.clearAddedByMember(memberId);
        groceryItemRepository.clearPurchasedByMember(memberId);
    }

    private RoomDto convertToRoomDto(RoomEntity room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setAddress(room.getAddress());
        dto.setDescription(room.getDescription());
        dto.setRoomCode(room.getRoomCode());
        dto.setHeadRoommateId(room.getHeadRoommateId());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());

        List<RoomMemberDto> memberDtos = new ArrayList<>();
        if (room.getMembers() != null) {
            for (RoomMemberEntity member : room.getMembers()) {
                if (member == null || member.getUser() == null)
                    continue;
                RoomMemberDto memberDto = new RoomMemberDto();
                memberDto.setId(member.getId());
                memberDto.setJoinedAt(member.getJoinedAt());
                memberDto.setUserId(member.getUser().getEmail());
                memberDto.setName(member.getUser().getFirstName());
                memberDto.setRole(member.getRole());
                memberDtos.add(memberDto);
            }
        }

        dto.setMembers(memberDtos.isEmpty() ? null : memberDtos);

        return dto;
    }

    private String generateUniqueRoomCode() {
        String roomCode;
        do {
            roomCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (roomRepository.existsByRoomCode(roomCode));
        return roomCode;
    }
}