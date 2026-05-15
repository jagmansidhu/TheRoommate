package com.roomate.app.entities.budget;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Maps to the budget_entries table — the same table n8n writes receipt/expense data to.
 * The backend can also insert manual entries here.
 */
@Getter
@Setter
@Entity
@Table(name = "budget_entries")
public class BudgetEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public BudgetEntryEntity() {
    }

    public BudgetEntryEntity(String userId, BigDecimal amount, String category,
                             String description, String status) {
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.status = status;
        this.submittedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
