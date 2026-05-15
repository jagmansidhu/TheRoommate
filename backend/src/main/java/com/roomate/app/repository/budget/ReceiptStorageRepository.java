package com.roomate.app.repository.budget;

import com.roomate.app.entities.budget.ReceiptStorageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiptStorageRepository extends JpaRepository<ReceiptStorageEntity, UUID> {
    Optional<ReceiptStorageEntity> findByReceiptId(UUID receiptId);
}
