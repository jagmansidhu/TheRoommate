package com.roomate.app.dto.ledger;

import com.roomate.app.dto.RoomMemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberBalanceDto {

    private UUID memberId;
    private RoomMemberDto member;
    private BigDecimal totalOwed;
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;
    private int unpaidSplitsCount;
}
