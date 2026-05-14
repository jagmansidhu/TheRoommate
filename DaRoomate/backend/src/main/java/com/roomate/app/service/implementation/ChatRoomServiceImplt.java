//package com.roomate.app.service.implementation;
//
//import com.roomate.app.entities.chatEntities.ChatRoomEntity;
//import com.roomate.app.repository.ChatRoomRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class ChatRoomServiceImplt {
//
//    private final ChatRoomRepository chatRoomRepository;
//
////    public Optional<String> getChatRoomId(
////            String senderId,
////            String recipientId,
////            boolean createNewRoomIfNotExists
////    ) {
////        return chatRoomRepository
////                .findBySenderIdAndRecipientId(senderId, recipientId)
////                .map(ChatRoomEntity::getChatId)
////                .or(() -> {
////                    if (createNewRoomIfNotExists) {
////                        var chatId = createChatId(senderId, recipientId, createNewRoomIfNotExists);
////                        return Optional.of(chatId);
////                    }
////
////                    return Optional.empty();
////                });
////    }
//
//    public Optional<String> getChatRoomId(String userA, String userB, boolean createNewRoomIfNotExists) {
//        String chatId = generateStableChatId(userA, userB);
//
//        return chatRoomRepository.findByChatId(chatId)
//                .map(ChatRoomEntity::getChatId)
//                .or(() -> {
//                    if (createNewRoomIfNotExists) {
//                        ChatRoomEntity room = ChatRoomEntity.builder()
//                                .chatId(chatId)
//                                .senderId(userA)
//                                .recipientId(userB)
//                                .build();
//                        chatRoomRepository.save(room);
//                        return Optional.of(chatId);
//                    }
//                    return Optional.empty();
//                });
//    }
//
//    private String generateStableChatId(String a, String b) {
//        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
//    }
//}