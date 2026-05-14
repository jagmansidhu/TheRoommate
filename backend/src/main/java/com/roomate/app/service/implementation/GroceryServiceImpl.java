package com.roomate.app.service.implementation;

import com.roomate.app.dto.grocery.*;
import com.roomate.app.entities.grocery.GroceryItemEntity;
import com.roomate.app.entities.grocery.GroceryListEntity;
import com.roomate.app.entities.grocery.GroceryListStatus;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.entities.room.RoomMemberEnum;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.*;
import com.roomate.app.service.GroceryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroceryServiceImpl implements GroceryService {

        private final GroceryListRepository groceryListRepository;
        private final GroceryItemRepository groceryItemRepository;
        private final RoomRepository roomRepository;
        private final RoomMemberRepository roomMemberRepository;

        @Override
        @Transactional
        public GroceryListDto createGroceryList(GroceryListCreateDto dto, String userEmail) {
                RoomEntity room = roomRepository.findById(dto.getRoomId())
                                .orElseThrow(() -> new UserApiError("Room not found"));

                RoomMemberEntity member = roomMemberRepository.findByRoomIdAndUserEmail(dto.getRoomId(), userEmail)
                                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

                GroceryListEntity list = new GroceryListEntity(room, dto.getName(), member);
                GroceryListEntity saved = groceryListRepository.save(list);
                return GroceryListDto.fromEntity(saved);
        }

        @Override
        public List<GroceryListDto> getGroceryListsForRoom(UUID roomId, String userEmail) {
                validateRoomMembership(roomId, userEmail);

                return groceryListRepository.findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                                .map(GroceryListDto::fromEntityWithoutItems)
                                .toList();
        }

        @Override
        public List<GroceryListDto> getActiveGroceryListsForRoom(UUID roomId, String userEmail) {
                validateRoomMembership(roomId, userEmail);

                return groceryListRepository.findActiveByRoomId(roomId).stream()
                                .map(GroceryListDto::fromEntityWithoutItems)
                                .toList();
        }

        @Override
        public GroceryListDto getGroceryListById(UUID listId, String userEmail) {
                GroceryListEntity list = groceryListRepository.findById(listId)
                                .orElseThrow(() -> new UserApiError("Grocery list not found"));

                validateRoomMembership(list.getRoom().getId(), userEmail);
                return GroceryListDto.fromEntity(list);
        }

        @Override
        @Transactional
        public GroceryItemDto addItem(UUID listId, GroceryItemCreateDto dto, String userEmail) {
                GroceryListEntity list = groceryListRepository.findById(listId)
                                .orElseThrow(() -> new UserApiError("Grocery list not found"));

                RoomMemberEntity member = roomMemberRepository
                                .findByRoomIdAndUserEmail(list.getRoom().getId(), userEmail)
                                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

                if (list.getStatus() != GroceryListStatus.ACTIVE) {
                        throw new UserApiError("Cannot add items to a completed or archived list");
                }

                GroceryItemEntity item = new GroceryItemEntity(list, dto.getName(), dto.getQuantity(),
                                dto.getCategory(),
                                member);
                item.setNotes(dto.getNotes());
                item.setEstimatedPrice(dto.getEstimatedPrice());

                list.getItems().add(item);
                groceryListRepository.save(list);

                return GroceryItemDto.fromEntity(item);
        }

        @Override
        @Transactional
        public GroceryItemDto updateItem(UUID itemId, GroceryItemCreateDto dto, String userEmail) {
                GroceryItemEntity item = groceryItemRepository.findById(itemId)
                                .orElseThrow(() -> new UserApiError("Grocery item not found"));

                validateRoomMembership(item.getGroceryList().getRoom().getId(), userEmail);

                if (item.isPurchased()) {
                        throw new UserApiError("Cannot update a purchased item");
                }

                item.setName(dto.getName());
                item.setQuantity(dto.getQuantity());
                item.setCategory(dto.getCategory());
                item.setNotes(dto.getNotes());
                item.setEstimatedPrice(dto.getEstimatedPrice());

                GroceryItemEntity saved = groceryItemRepository.save(item);
                return GroceryItemDto.fromEntity(saved);
        }

        @Override
        @Transactional
        public GroceryItemDto markItemPurchased(UUID itemId, PurchaseItemDto dto, String userEmail) {
                GroceryItemEntity item = groceryItemRepository.findById(itemId)
                                .orElseThrow(() -> new UserApiError("Grocery item not found"));

                RoomMemberEntity member = roomMemberRepository.findByRoomIdAndUserEmail(
                                item.getGroceryList().getRoom().getId(), userEmail)
                                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

                item.markPurchased(member, dto.getActualPrice());
                GroceryItemEntity saved = groceryItemRepository.save(item);
                return GroceryItemDto.fromEntity(saved);
        }

        @Override
        @Transactional
        public GroceryItemDto unmarkItemPurchased(UUID itemId, String userEmail) {
                GroceryItemEntity item = groceryItemRepository.findById(itemId)
                                .orElseThrow(() -> new UserApiError("Grocery item not found"));

                validateRoomMembership(item.getGroceryList().getRoom().getId(), userEmail);

                item.unmarkPurchased();
                GroceryItemEntity saved = groceryItemRepository.save(item);
                return GroceryItemDto.fromEntity(saved);
        }

        @Override
        @Transactional
        public void removeItem(UUID itemId, String userEmail) {
                GroceryItemEntity item = groceryItemRepository.findById(itemId)
                                .orElseThrow(() -> new UserApiError("Grocery item not found"));

                RoomMemberEntity member = roomMemberRepository.findByRoomIdAndUserEmail(
                                item.getGroceryList().getRoom().getId(), userEmail)
                                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

                boolean isAdder = item.getAddedBy() != null && item.getAddedBy().getId().equals(member.getId());
                boolean isHeadRoommate = member.getRole() == RoomMemberEnum.HEAD_ROOMMATE;

                if (!isAdder && !isHeadRoommate) {
                        throw new UserApiError("You can only remove items you added");
                }

                groceryItemRepository.delete(item);
        }

        @Override
        @Transactional
        public GroceryListDto completeList(UUID listId, String userEmail) {
                GroceryListEntity list = groceryListRepository.findById(listId)
                                .orElseThrow(() -> new UserApiError("Grocery list not found"));

                validateRoomMembership(list.getRoom().getId(), userEmail);

                list.complete();
                GroceryListEntity saved = groceryListRepository.save(list);
                return GroceryListDto.fromEntity(saved);
        }

        @Override
        @Transactional
        public GroceryListDto archiveList(UUID listId, String userEmail) {
                GroceryListEntity list = groceryListRepository.findById(listId)
                                .orElseThrow(() -> new UserApiError("Grocery list not found"));

                RoomMemberEntity member = roomMemberRepository
                                .findByRoomIdAndUserEmail(list.getRoom().getId(), userEmail)
                                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

                if (member.getRole() != RoomMemberEnum.HEAD_ROOMMATE) {
                        throw new UserApiError("Only head roommates can archive grocery lists");
                }

                list.archive();
                GroceryListEntity saved = groceryListRepository.save(list);
                return GroceryListDto.fromEntity(saved);
        }

        @Override
        @Transactional
        public void deleteList(UUID listId, String userEmail) {
                GroceryListEntity list = groceryListRepository.findById(listId)
                                .orElseThrow(() -> new UserApiError("Grocery list not found"));

                RoomMemberEntity member = roomMemberRepository
                                .findByRoomIdAndUserEmail(list.getRoom().getId(), userEmail)
                                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

                boolean isHeadRoommate = member.getRole() == RoomMemberEnum.HEAD_ROOMMATE;
                boolean isCreator = list.getCreatedBy() != null && list.getCreatedBy().getId().equals(member.getId());

                if (!isHeadRoommate && !isCreator) {
                        throw new UserApiError("You don't have permission to delete this list");
                }

                groceryListRepository.delete(list);
        }

        private RoomMemberEntity validateRoomMembership(UUID roomId, String userEmail) {
                return roomMemberRepository.findByRoomIdAndUserEmail(roomId, userEmail)
                                .orElseThrow(() -> new UserApiError("You are not a member of this room"));
        }
}
