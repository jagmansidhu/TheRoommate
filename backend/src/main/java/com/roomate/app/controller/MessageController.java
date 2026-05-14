//package com.roomate.app.controller;
//
//import com.roomate.app.dto.UserDTOS.UserDto;
//import com.roomate.app.entities.UserEntity;
//import com.roomate.app.entities.chatEntities.ChatNotificationEntity;
//import com.roomate.app.entities.chatEntities.MessageEntity;
//import com.roomate.app.repository.UserRepository;
//import com.roomate.app.service.implementation.MessageServiceImplt;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")
//@RequestMapping("/api/messages/")
//public class MessageController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final MessageServiceImplt chatMessageService;
//    private final UserRepository userRepository;
//
//    @MessageMapping("/chat")
//    public void processMessage(@Payload MessageEntity chatMessage) {
//        if (!isValidEmail(chatMessage.getSenderId()) || !isValidEmail(chatMessage.getRecipientId())) {
//            throw new IllegalArgumentException("Malformed sender or recipient email");
//        }
//
//        System.out.println("DEBUG: Entering processMessage in MessageController.");
//        System.out.println("DEBUG: Message received - Sender: " + chatMessage.getSenderId() +
//                ", Recipient: " + chatMessage.getRecipientId() +
//                ", Content: " + chatMessage.getContent());
//
//        MessageEntity savedMsg = chatMessageService.save(chatMessage);
//        messagingTemplate.convertAndSendToUser(
//                chatMessage.getRecipientId(), "/queue/messages",
//                new ChatNotificationEntity(
//                        savedMsg.getId(),
//                        savedMsg.getSenderId(),
//                        savedMsg.getRecipientId(),
//                        savedMsg.getContent()
//                )
//        );
//    }
//
//    private boolean isValidEmail(String senderId) {
//        return senderId != null && senderId.contains("@") && !senderId.contains("_");
//    }
//
//    @GetMapping("/{senderId}/{recipientId}")
//    public ResponseEntity<List<MessageEntity>> findChatMessages(
//            @PathVariable String senderId,
//            @PathVariable String recipientId) {
//        return ResponseEntity.ok(chatMessageService.findMessageEntitys(senderId, recipientId));
//    }
//
//    @GetMapping("/unread/{userId}")
//    public ResponseEntity<Long> countUnreadMessages(@PathVariable String userId) {
//        return ResponseEntity.ok(chatMessageService.countUnreadMessages(userId));
//    }
//
//    @PutMapping("/read/{messageId}")
//    public ResponseEntity<Void> markMessageAsRead(@PathVariable String messageId) {
//        chatMessageService.markMessageAsRead(messageId);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/chats/{userId}")
//    public ResponseEntity<List<String>> findUserChats(@PathVariable String userId) {
//        return ResponseEntity.ok(chatMessageService.findUserChats(userId));
//    }
//
//    @GetMapping("/users/{userId}")
//    public ResponseEntity<List<UserDto>> getAllChatUsers(@PathVariable String userId) {
//        try {
//            List<UserDto> users = chatMessageService.findAllFriends(userId);
//            return ResponseEntity.ok(users);
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching all chat users", e);
//        }
//    }
//
//
//    @DeleteMapping("/{messageId}")
//    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
//        chatMessageService.deleteMessage(messageId);
//        return ResponseEntity.ok().build();
//    }
//
//
//}