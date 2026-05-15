package com.roomate.app.service;

import com.roomate.app.dto.BudgetEntryDto;
import com.roomate.app.dto.BudgetSettingsDto;
import com.roomate.app.dto.BudgetStatsDto;
import com.roomate.app.dto.CreateBudgetEntryRequest;

import java.util.List;
import java.util.UUID;

public interface BudgetService {
    List<BudgetEntryDto> getEntries(String email);
    List<BudgetEntryDto> getEntriesByMonth(String email, int year, int month);
    BudgetStatsDto getStats(String email, int year, int month);
    BudgetEntryDto createEntry(String email, CreateBudgetEntryRequest request);
    void updateEntryCategory(UUID entryId, String email, String newCategory);
    void deleteEntry(UUID entryId, String email);
    BudgetSettingsDto getBudgetSettings(String email);
    void setBudgetSettings(String email, BudgetSettingsDto settings);
}
