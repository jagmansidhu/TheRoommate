package com.roomate.app.repository;

import com.roomate.app.entities.ledger.LedgerSplitEntity;
import com.roomate.app.entities.ledger.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerSplitRepository extends JpaRepository<LedgerSplitEntity, UUID> {

    /**
     * Find all splits for a specific member
     */
    List<LedgerSplitEntity> findByRoomMemberId(UUID memberId);

    /**
     * Find all splits for a member with a specific payment status
     */
    List<LedgerSplitEntity> findByRoomMemberIdAndPaymentStatus(UUID memberId, PaymentStatus status);

    /**
     * Find unpaid splits for a member
     */
    @Query("SELECT s FROM LedgerSplitEntity s WHERE s.roomMember.id = :memberId AND s.paymentStatus != 'PAID'")
    List<LedgerSplitEntity> findUnpaidByMemberId(@Param("memberId") UUID memberId);

    /**
     * Find all splits for a ledger entry
     */
    List<LedgerSplitEntity> findByLedgerEntryId(UUID ledgerEntryId);

    /**
     * Calculate total owed by a member across all unpaid splits
     */
    @Query("SELECT COALESCE(SUM(s.amountOwed - s.amountPaid), 0) FROM LedgerSplitEntity s " +
            "WHERE s.roomMember.id = :memberId AND s.paymentStatus != 'PAID'")
    BigDecimal calculateOutstandingBalance(@Param("memberId") UUID memberId);

    /**
     * Find splits for a member in a specific room
     */
    @Query("SELECT s FROM LedgerSplitEntity s WHERE s.roomMember.id = :memberId " +
            "AND s.ledgerEntry.room.id = :roomId ORDER BY s.ledgerEntry.createdAt DESC")
    List<LedgerSplitEntity> findByMemberIdAndRoomId(@Param("memberId") UUID memberId, @Param("roomId") UUID roomId);
}
