package com.roomate.app.repository;

import com.roomate.app.entities.ledger.LedgerEntryEntity;
import com.roomate.app.entities.ledger.LedgerEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    /**
     * Find all ledger entries for a room, ordered by creation date (newest first)
     */
    List<LedgerEntryEntity> findByRoomIdOrderByCreatedAtDesc(UUID roomId);

    /**
     * Find ledger entries for a room with a specific status
     */
    List<LedgerEntryEntity> findByRoomIdAndStatus(UUID roomId, LedgerEntryStatus status);

    /**
     * Find ledger entries for a room that are not cancelled
     */
    @Query("SELECT e FROM LedgerEntryEntity e WHERE e.room.id = :roomId AND e.status != 'CANCELLED' ORDER BY e.createdAt DESC")
    List<LedgerEntryEntity> findActiveByRoomId(@Param("roomId") UUID roomId);

    /**
     * Find pending entries (awaiting split assignment)
     */
    List<LedgerEntryEntity> findByRoomIdAndStatusOrderByCreatedAtDesc(UUID roomId, LedgerEntryStatus status);

    /**
     * Count entries by room and status
     */
    long countByRoomIdAndStatus(UUID roomId, LedgerEntryStatus status);

    @Modifying
    @Query("DELETE FROM LedgerEntryEntity e WHERE e.room.id = :roomId")
    void deleteAllByRoomId(@Param("roomId") UUID roomId);
}
