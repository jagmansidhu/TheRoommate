package com.roomate.app.dto;

import java.math.BigDecimal;

public class CreateBudgetEntryRequest {
    public BigDecimal amount;
    public String category;
    public String description;
    public String status;
    public java.time.Instant paymentDate;
}
