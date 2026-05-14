package com.roomate.app.entities.ledger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ledger_entry")
public class LedgerEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_member_id")
    private RoomMemberEntity createdBy;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryType entryType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitType splitType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryStatus status = LedgerEntryStatus.PENDING;

    private LocalDate dueDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ledgerEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LedgerSplitEntity> splits = new ArrayList<>();

    public LedgerEntryEntity(RoomEntity room, RoomMemberEntity createdBy, String title,
            LedgerEntryType entryType, BigDecimal totalAmount, SplitType splitType) {
        this.room = room;
        this.createdBy = createdBy;
        this.title = title;
        this.entryType = entryType;
        this.totalAmount = totalAmount;
        this.splitType = splitType;
        this.status = LedgerEntryStatus.PENDING;
    }

    public BigDecimal getTotalPaid() {
        return splits.stream()
                .map(LedgerSplitEntity::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRemainingBalance() {
        return totalAmount.subtract(getTotalPaid());
    }

    public boolean isFullyPaid() {
        return getTotalPaid().compareTo(totalAmount) >= 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
