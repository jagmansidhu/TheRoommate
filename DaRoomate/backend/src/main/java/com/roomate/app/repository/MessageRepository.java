//package com.roomate.app.repository;
//
//import com.roomate.app.entities.chatEntities.MessageEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface MessageRepository extends JpaRepository<MessageEntity, String> {
//
//    List<MessageEntity> findByChatIdOrderByTimestampAsc(String chatId);
//    Long countByRecipientIdAndIsReadFalse(String recipientId);
//
//    @Query("SELECT DISTINCT m.chatId FROM MessageEntity m WHERE m.senderId = :userId OR m.recipientId = :userId")
//    List<String> findDistinctChatIdsBySenderIdOrRecipientId(@Param("userId") String userId);
//
//    @Query("SELECT m.senderId, COUNT(m) FROM MessageEntity m " +
//            "WHERE m.recipientId = :recipientId AND m.isRead = FALSE " +
//            "GROUP BY m.senderId")
//    List<Object[]> countUnreadMessagesGroupedBySender(@Param("recipientId") String recipientId);
//
//    @Modifying
//    @Query("UPDATE MessageEntity m SET m.isRead = TRUE WHERE m.chatId = :chatId " +
//            "AND m.senderId = :senderId AND m.recipientId = :recipientId AND m.isRead = FALSE")
//    int markMessagesAsReadByChatIdAndSenderIdAndRecipientId(
//            @Param("chatId") String chatId,
//            @Param("senderId") String senderId,
//            @Param("recipientId") String recipientId
//    );
//}
