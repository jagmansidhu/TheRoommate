package com.roomate.app.dto.grocery;

import com.roomate.app.dto.RoomMemberDto;
import com.roomate.app.entities.grocery.GroceryItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroceryItemDto {

    private UUID id;
    private UUID groceryListId;
    private String name;
    private String quantity;
    private String category;
    private String notes;
    private boolean isPurchased;
    private RoomMemberDto addedBy;
    private RoomMemberDto purchasedBy;
    private BigDecimal estimatedPrice;
    private BigDecimal actualPrice;
    private LocalDateTime purchasedAt;
    private LocalDateTime createdAt;

    public static GroceryItemDto fromEntity(GroceryItemEntity entity) {
        return GroceryItemDto.builder()
                .id(entity.getId())
                .groceryListId(entity.getGroceryList().getId())
                .name(entity.getName())
                .quantity(entity.getQuantity())
                .category(entity.getCategory())
                .notes(entity.getNotes())
                .isPurchased(entity.isPurchased())
                .addedBy(entity.getAddedBy() != null ? RoomMemberDto.fromEntity(entity.getAddedBy()) : null)
                .purchasedBy(entity.getPurchasedBy() != null ? RoomMemberDto.fromEntity(entity.getPurchasedBy()) : null)
                .estimatedPrice(entity.getEstimatedPrice())
                .actualPrice(entity.getActualPrice())
                .purchasedAt(entity.getPurchasedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
