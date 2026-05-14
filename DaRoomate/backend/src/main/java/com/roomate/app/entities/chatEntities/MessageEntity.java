//package com.roomate.app.entities.chatEntities;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Column;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Date;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Entity
//public class MessageEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private String id;
//
//    @Column(nullable = false)
//    private String chatId;
//
//    @Column(nullable = false)
//    private String senderId;
//
//    @Column(nullable = false)
//    private String recipientId;
//
//    @Column(nullable = false, length = 1000)
//    private String content;
//
//    @Column(nullable = false)
//    private Date timestamp;
//
//    @Column(nullable = false)
//    private boolean isRead;
//
//    @Column
//    private String messageType; // TEXT, IMAGE, FILE
//
//    @Column
//    private String attachmentUrl; // For images and files
//
//    @Column
//    private String attachmentName; // Original filename for attachments
//
//    @Column
//    private Long attachmentSize; // Size in bytes for attachments
//}