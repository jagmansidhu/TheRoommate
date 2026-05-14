package com.roomate.app.dto.ledger;

import com.roomate.app.entities.ledger.LedgerEntryType;
import com.roomate.app.entities.ledger.SplitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryCreateDto {

    @NotNull(message = "Room ID is required")
    private UUID roomId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Entry type is required")
    private LedgerEntryType entryType;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    private LocalDate dueDate;
}
