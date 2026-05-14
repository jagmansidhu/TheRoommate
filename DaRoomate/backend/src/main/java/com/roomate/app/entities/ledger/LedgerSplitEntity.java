package com.roomate.app.entities.ledger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roomate.app.entities.room.RoomMemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ledger_split")
public class LedgerSplitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_entry_id", nullable = false)
    @JsonIgnore
    private LedgerEntryEntity ledgerEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_member_id", nullable = false)
    private RoomMemberEntity roomMember;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountOwed;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private LocalDateTime paidAt;

    @Column(length = 500)
    private String notes;

    public LedgerSplitEntity(LedgerEntryEntity ledgerEntry, RoomMemberEntity roomMember, BigDecimal amountOwed) {
        this.ledgerEntry = ledgerEntry;
        this.roomMember = roomMember;
        this.amountOwed = amountOwed;
        this.amountPaid = BigDecimal.ZERO;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    public void recordPayment(BigDecimal amount) {
        this.amountPaid = this.amountPaid.add(amount);
        updatePaymentStatus();
    }

    private void updatePaymentStatus() {
        if (amountPaid.compareTo(BigDecimal.ZERO) == 0) {
            this.paymentStatus = PaymentStatus.UNPAID;
        } else if (amountPaid.compareTo(amountOwed) >= 0) {
            this.paymentStatus = PaymentStatus.PAID;
            this.paidAt = LocalDateTime.now();
        } else {
            this.paymentStatus = PaymentStatus.PARTIAL;
        }
    }

    public BigDecimal getRemainingBalance() {
        return amountOwed.subtract(amountPaid);
    }

    public boolean isFullyPaid() {
        return amountPaid.compareTo(amountOwed) >= 0;
    }
}
