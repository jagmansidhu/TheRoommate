package com.roomate.app.dto.grocery;

import com.roomate.app.dto.RoomMemberDto;
import com.roomate.app.entities.grocery.GroceryListEntity;
import com.roomate.app.entities.grocery.GroceryListStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroceryListDto {

    private UUID id;
    private UUID roomId;
    private String name;
    private GroceryListStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private RoomMemberDto createdBy;
    private List<GroceryItemDto> items;
    private long totalItems;
    private long purchasedItems;
    private long remainingItems;
    private BigDecimal totalSpent;

    public static GroceryListDto fromEntity(GroceryListEntity entity) {
        return GroceryListDto.builder()
                .id(entity.getId())
                .roomId(entity.getRoom().getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .createdBy(entity.getCreatedBy() != null ? RoomMemberDto.fromEntity(entity.getCreatedBy()) : null)
                .items(entity.getItems().stream().map(GroceryItemDto::fromEntity).toList())
                .totalItems(entity.getItems().size())
                .purchasedItems(entity.getPurchasedCount())
                .remainingItems(entity.getRemainingCount())
                .totalSpent(entity.getTotalSpent())
                .build();
    }

    public static GroceryListDto fromEntityWithoutItems(GroceryListEntity entity) {
        return GroceryListDto.builder()
                .id(entity.getId())
                .roomId(entity.getRoom().getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .createdBy(entity.getCreatedBy() != null ? RoomMemberDto.fromEntity(entity.getCreatedBy()) : null)
                .totalItems(entity.getItems().size())
                .purchasedItems(entity.getPurchasedCount())
                .remainingItems(entity.getRemainingCount())
                .totalSpent(entity.getTotalSpent())
                .build();
    }
}
