package com.roomate.app.dto;

import java.math.BigDecimal;
import java.util.Map;

public class BudgetStatsDto {
    public BigDecimal totalSpent;
    public Map<String, BigDecimal> spentByCategory;
    public BigDecimal monthlyBudget;
    public BigDecimal remainingBudget;

    public BudgetStatsDto(BigDecimal totalSpent, Map<String, BigDecimal> spentByCategory, BigDecimal monthlyBudget, BigDecimal remainingBudget) {
        this.totalSpent = totalSpent;
        this.spentByCategory = spentByCategory;
        this.monthlyBudget = monthlyBudget;
        this.remainingBudget = remainingBudget;
    }
}
