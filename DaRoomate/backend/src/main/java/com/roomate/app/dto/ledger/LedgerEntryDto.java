package com.roomate.app.dto.ledger;

import com.roomate.app.dto.RoomMemberDto;
import com.roomate.app.entities.ledger.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryDto {

    private UUID id;
    private UUID roomId;
    private RoomMemberDto createdBy;
    private String title;
    private String description;
    private LedgerEntryType entryType;
    private BigDecimal totalAmount;
    private SplitType splitType;
    private LedgerEntryStatus status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LedgerSplitDto> splits;
    private BigDecimal totalPaid;
    private BigDecimal remainingBalance;

    public static LedgerEntryDto fromEntity(LedgerEntryEntity entity) {
        return LedgerEntryDto.builder()
                .id(entity.getId())
                .roomId(entity.getRoom().getId())
                .createdBy(entity.getCreatedBy() != null ? RoomMemberDto.fromEntity(entity.getCreatedBy()) : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .entryType(entity.getEntryType())
                .totalAmount(entity.getTotalAmount())
                .splitType(entity.getSplitType())
                .status(entity.getStatus())
                .dueDate(entity.getDueDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .splits(entity.getSplits().stream().map(LedgerSplitDto::fromEntity).toList())
                .totalPaid(entity.getTotalPaid())
                .remainingBalance(entity.getRemainingBalance())
                .build();
    }
}
