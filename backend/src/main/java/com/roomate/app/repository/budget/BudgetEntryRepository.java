package com.roomate.app.repository.budget;

import com.roomate.app.entities.budget.BudgetEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetEntryRepository extends JpaRepository<BudgetEntryEntity, UUID> {

    List<BudgetEntryEntity> findByUserIdOrderBySubmittedAtDesc(String userId);

    List<BudgetEntryEntity> findByUserIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
            String userId, Instant start, Instant end);
}
