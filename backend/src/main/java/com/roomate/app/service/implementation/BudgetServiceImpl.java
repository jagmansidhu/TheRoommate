package com.roomate.app.service.implementation;

import com.roomate.app.dto.BudgetEntryDto;
import com.roomate.app.dto.BudgetSettingsDto;
import com.roomate.app.dto.BudgetStatsDto;
import com.roomate.app.dto.CreateBudgetEntryRequest;
import com.roomate.app.entities.budget.BudgetEntryEntity;
import com.roomate.app.entities.budget.UserBudgetEntity;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.budget.BudgetEntryRepository;
import com.roomate.app.repository.budget.UserBudgetRepository;
import com.roomate.app.service.BudgetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetEntryRepository budgetEntryRepository;
    private final UserBudgetRepository userBudgetRepository;

    public BudgetServiceImpl(BudgetEntryRepository budgetEntryRepository, UserBudgetRepository userBudgetRepository) {
        this.budgetEntryRepository = budgetEntryRepository;
        this.userBudgetRepository = userBudgetRepository;
    }

    private BudgetEntryDto mapToDto(BudgetEntryEntity entity) {
        return new BudgetEntryDto(
                entity.getId(),
                entity.getAmount(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getSubmittedAt()
        );
    }

    @Override
    @Transactional("budgetTransactionManager")
    public List<BudgetEntryDto> getEntries(String email) {
        return budgetEntryRepository.findByUserIdOrderBySubmittedAtDesc(email).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional("budgetTransactionManager")
    public List<BudgetEntryDto> getEntriesByMonth(String email, int year, int month) {
        ZonedDateTime startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        return budgetEntryRepository.findByUserIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                        email, startOfMonth.toInstant(), endOfMonth.toInstant()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional("budgetTransactionManager")
    public BudgetStatsDto getStats(String email, int year, int month) {
        List<BudgetEntryEntity> entries;
        if (year > 0 && month > 0) {
            ZonedDateTime startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.systemDefault());
            ZonedDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
            entries = budgetEntryRepository.findByUserIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                    email, startOfMonth.toInstant(), endOfMonth.toInstant());
        } else {
             // Default to current month if not specified
             ZonedDateTime startOfMonth = ZonedDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
             ZonedDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
             entries = budgetEntryRepository.findByUserIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                     email, startOfMonth.toInstant(), endOfMonth.toInstant());
        }

        BigDecimal totalSpent = entries.stream()
                .map(BudgetEntryEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> spentByCategory = entries.stream()
                .collect(Collectors.groupingBy(
                        BudgetEntryEntity::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, BudgetEntryEntity::getAmount, BigDecimal::add)
                ));

        BigDecimal monthlyBudget = userBudgetRepository.findByUserId(email)
                .map(UserBudgetEntity::getMonthlyBudget)
                .orElse(BigDecimal.ZERO);

        BigDecimal remainingBudget = monthlyBudget.subtract(totalSpent);

        return new BudgetStatsDto(totalSpent, spentByCategory, monthlyBudget, remainingBudget);
    }

    @Override
    @Transactional("budgetTransactionManager")
    public BudgetEntryDto createEntry(String email, CreateBudgetEntryRequest request) {
        BudgetEntryEntity entity = new BudgetEntryEntity(
                email,
                request.amount,
                request.category != null ? request.category : "Other",
                request.description,
                request.status
        );
        BudgetEntryEntity saved = budgetEntryRepository.save(entity);
        return mapToDto(saved);
    }

    @Override
    @Transactional("budgetTransactionManager")
    public void updateEntryCategory(UUID entryId, String email, String newCategory) {
        BudgetEntryEntity entity = budgetEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Entry not found"));
        
        if (!entity.getUserId().equals(email)) {
            throw new UserApiError("Unauthorized to update this entry");
        }

        entity.setCategory(newCategory);
        budgetEntryRepository.save(entity);
    }

    @Override
    @Transactional("budgetTransactionManager")
    public void deleteEntry(UUID entryId, String email) {
        BudgetEntryEntity entity = budgetEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Entry not found"));

        if (!entity.getUserId().equals(email)) {
            throw new UserApiError("Unauthorized to delete this entry");
        }

        budgetEntryRepository.delete(entity);
    }

    @Override
    @Transactional("budgetTransactionManager")
    public BudgetSettingsDto getBudgetSettings(String email) {
        BudgetSettingsDto dto = new BudgetSettingsDto();
        dto.monthlyBudget = userBudgetRepository.findByUserId(email)
                .map(UserBudgetEntity::getMonthlyBudget)
                .orElse(BigDecimal.ZERO);
        return dto;
    }

    @Override
    @Transactional("budgetTransactionManager")
    public void setBudgetSettings(String email, BudgetSettingsDto settings) {
        UserBudgetEntity entity = userBudgetRepository.findByUserId(email)
                .orElse(new UserBudgetEntity(email, settings.monthlyBudget));
        
        entity.setMonthlyBudget(settings.monthlyBudget);
        userBudgetRepository.save(entity);
    }
}
