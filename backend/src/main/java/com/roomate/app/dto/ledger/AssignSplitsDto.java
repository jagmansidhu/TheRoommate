package com.roomate.app.dto.ledger;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignSplitsDto {

    @NotEmpty(message = "At least one split assignment is required")
    private List<SplitAssignment> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitAssignment {
        @NotNull(message = "Member ID is required")
        private UUID memberId;

        @NotNull(message = "Amount is required")
        private BigDecimal amount;

        private String notes;
    }
}
