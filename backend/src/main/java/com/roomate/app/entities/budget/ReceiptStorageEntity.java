package com.roomate.app.entities.budget;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Maps to the receipt_storage table — stores S3 file metadata from n8n.
 */
@Getter
@Setter
@Entity
@Table(name = "receipt_storage")
public class ReceiptStorageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "receipt_id", nullable = false)
    private UUID receiptId;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "s3_bucket_name")
    private String s3BucketName;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    public ReceiptStorageEntity() {
    }
}
