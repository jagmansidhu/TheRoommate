package com.roomate.app.dto.ledger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Valid
    private List<SplitAssignment> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitAssignment {
        @NotNull(message = "Member ID is required")
        private UUID memberId;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        private BigDecimal amount;

        private String notes;
    }
}
