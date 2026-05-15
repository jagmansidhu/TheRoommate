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
    public String s3Url;
    public Instant paymentDate;
    public Instant submittedAt;

    public BudgetEntryDto(UUID id, BigDecimal amount, String category, String description, String status, String s3Url, Instant paymentDate, Instant submittedAt) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.status = status;
        this.s3Url = s3Url;
        this.paymentDate = paymentDate;
        this.submittedAt = submittedAt;
    }
}
