//package com.roomate.app.repository;
//
//import com.roomate.app.entities.chatEntities.ChatRoomEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
//@Repository
//public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, String> {
//    Optional<ChatRoomEntity> findBySenderIdAndRecipientId(String senderId, String recipientId);
//    Optional<ChatRoomEntity> findByChatId(String chatId);
//
//
//}
