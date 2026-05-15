package com.roomate.app.entities.budget;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Stores each user's monthly budget target.
 * One row per user — upserted when the user changes their budget.
 */
@Getter
@Setter
@Entity
@Table(name = "user_budgets")
public class UserBudgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "monthly_budget", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyBudget;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserBudgetEntity() {
    }

    public UserBudgetEntity(String userId, BigDecimal monthlyBudget) {
        this.userId = userId;
        this.monthlyBudget = monthlyBudget;
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
