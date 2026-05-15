package com.roomate.app.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class BudgetEntryDto {
    public UUID id;
    public BigDecimal amount;
    public String category;
    public String description;
    public String status;
    public Instant submittedAt;

    public BudgetEntryDto(UUID id, BigDecimal amount, String category, String description, String status, Instant submittedAt) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.status = status;
        this.submittedAt = submittedAt;
    }
}
