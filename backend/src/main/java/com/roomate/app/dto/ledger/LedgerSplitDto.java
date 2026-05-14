package com.roomate.app.dto.ledger;

import com.roomate.app.dto.RoomMemberDto;
import com.roomate.app.entities.ledger.LedgerSplitEntity;
import com.roomate.app.entities.ledger.PaymentStatus;
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
public class LedgerSplitDto {

    private UUID id;
    private UUID ledgerEntryId;
    private RoomMemberDto roomMember;
    private BigDecimal amountOwed;
    private BigDecimal amountPaid;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private String notes;
    private BigDecimal remainingBalance;

    public static LedgerSplitDto fromEntity(LedgerSplitEntity entity) {
        return LedgerSplitDto.builder()
                .id(entity.getId())
                .ledgerEntryId(entity.getLedgerEntry().getId())
                .roomMember(RoomMemberDto.fromEntity(entity.getRoomMember()))
                .amountOwed(entity.getAmountOwed())
                .amountPaid(entity.getAmountPaid())
                .paymentStatus(entity.getPaymentStatus())
                .paidAt(entity.getPaidAt())
                .notes(entity.getNotes())
                .remainingBalance(entity.getRemainingBalance())
                .build();
    }
}
