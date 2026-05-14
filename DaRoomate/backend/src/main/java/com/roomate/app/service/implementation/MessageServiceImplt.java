//package com.roomate.app.service.implementation;
//
//import com.roomate.app.dto.UserDTOS.UserDto;
//import com.roomate.app.entities.chatEntities.MessageEntity;
//import com.roomate.app.repository.FriendRepository;
//import com.roomate.app.repository.MessageRepository;
//import com.roomate.app.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class MessageServiceImplt {
//
//    private final MessageRepository messageRepository;
//    private final ChatRoomServiceImplt chatRoomService;
//    private final UserRepository userRepository;
//    private final FriendRepository friendRepository;
//
//    @Transactional
//    public MessageEntity save(MessageEntity messageEntity) {
//        System.out.println("DEBUG: Entering save method in MessageServiceImplt.");
//
//        var chatId = chatRoomService
//                .getChatRoomId(messageEntity.getSenderId(), messageEntity.getRecipientId(), true)
//                .orElseThrow(() -> {
//                    System.err.println("ERROR: Failed to create or retrieve chat ID for sender: " +
//                            messageEntity.getSenderId() + ", recipient: " + messageEntity.getRecipientId());
//                    return new IllegalStateException("Failed to create or retrieve chat ID");
//                });
//        System.out.println("DEBUG: Chat ID obtained: " + chatId); // New debug log
//
//        messageEntity.setChatId(chatId);
//        messageEntity.setTimestamp(new Date());
//        messageEntity.setRead(false);
//        if (messageEntity.getMessageType() == null || messageEntity.getMessageType().isEmpty()) {
//            messageEntity.setMessageType("TEXT");
//        }
//        System.out.println("DEBUG: MessageEntity before save - Sender: " + messageEntity.getSenderId() +
//                ", Recipient: " + messageEntity.getRecipientId() +
//                ", ChatId: " + messageEntity.getChatId() +
//                ", Content: " + messageEntity.getContent());
//
//        try {
//            MessageEntity savedMessage = messageRepository.save(messageEntity);
//            System.out.println("DEBUG: Message saved successfully. ID: " + savedMessage.getId() + ", Content: " + savedMessage.getContent());
//            return savedMessage;
//        } catch (Exception e) {
//            System.err.println("ERROR: Exception occurred during messageRepository.save(): " + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException("Error saving message to database", e);
//        }
//    }
//
//    public List<MessageEntity> findMessageEntitys(String senderId, String recipientId) {
//        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
//        return chatId.map(messageRepository::findByChatIdOrderByTimestampAsc).orElse(new ArrayList<>());
//    }
//
//    public Long countUnreadMessages(String userId) {
//        return messageRepository.countByRecipientIdAndIsReadFalse(userId);
//    }
//
//    @Transactional
//    public void markMessageAsRead(String messageId) {
//        messageRepository.findById(messageId).ifPresent(message -> {
//            message.setRead(true);
//            messageRepository.save(message);
//        });
//    }
//
////    @Transactional
////    public void markMessagesAsReadBetweenUsers(String senderId, String recipientId) {
////        Optional<String> chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
////        if (chatId.isPresent()) {
////            messageRepository.markMessagesAsReadByChatIdAndSenderIdAndRecipientId(chatId.get(), senderId, recipientId);
////        }
////    }
//
//    public List<String> findUserChats(String userId) {
//        return messageRepository.findDistinctChatIdsBySenderIdOrRecipientId(userId);
//    }
//
//    @Transactional
//    public void deleteMessage(String messageId) {
//        messageRepository.deleteById(messageId);
//    }
////
////    public Map<String, Long> countUnreadMessagesPerSender(String userId) {
////        List<Object[]> unreadCountsRaw = messageRepository.countUnreadMessagesGroupedBySender(userId);
////        return unreadCountsRaw.stream()
////                .collect(Collectors.toMap(
////                        arr -> (String) arr[0],
////                        arr -> (Long) arr[1]
////                ));
////    }
//
//    public List<UserDto> findAllFriends(String userId) {
//        return friendRepository.findAll().stream()
////                .filter(friend -> friend.getStatus() == FriendEnum.ACCEPTED)
//                .map(friend -> {
//                    String requesterEmail = friend.getRequester().getEmail();
//                    String addresseeEmail = friend.getAddressee().getEmail();
//                    System.out.println(requesterEmail);
//                    System.out.println(addresseeEmail);
//                    if (friend.getRequester() == null || friend.getAddressee() == null) {
//                        System.err.println("ERROR: Found a friend relationship with null requester or addressee. Friend ID: " + friend.getId());
//                        return null;
//                    }
//
//                    if (requesterEmail.equals(userId)) {
//                        return new UserDto(addresseeEmail);
//                    } else if (addresseeEmail.equals(userId)) {
//                        return new UserDto(requesterEmail);
//                    }
//                    return null;
//                })
//                .filter(java.util.Objects::nonNull)
//                .collect(Collectors.toList());
//    }
//
//
//}
