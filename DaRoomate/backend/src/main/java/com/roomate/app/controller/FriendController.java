//package com.roomate.app.controller;
//
//import com.roomate.app.dto.UserDTOS.UserDto;
//import com.roomate.app.entities.friendEntity.FriendEntity;
//import com.roomate.app.repository.UserRepository;
//import com.roomate.app.service.FriendService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//@RequestMapping("/api/friend/")
//public class FriendController {
//
//    FriendService friendService;
//
//    public FriendController(FriendService friendService) {
//        this.friendService = friendService;
//    }
//
//    @PostMapping("/addFriend")
//    public ResponseEntity<?> addFriend(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, String> request) {
//        String authId = jwt.getSubject();
//
//        if (request == null) {
//            return ResponseEntity.badRequest().build();
//        }
//        String friendEmail = request.get("email");
//
//        try {
//            FriendEntity friendRequest = friendService.sendFriendRequest(authId, friendEmail);
//            return ResponseEntity.ok(friendRequest);
//        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("message", e.getMessage());
//            return ResponseEntity.badRequest().body(error);
//        }
//    }
//
//    @PostMapping("/accept/{requestId}")
//    public ResponseEntity<?> acceptFriendRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable Long requestId) {
//
//        String authId = jwt.getSubject();
//
//        try {
//            friendService.acceptFriendRequest(requestId, authId);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @PostMapping("/reject/{requestId}")
//    public ResponseEntity<?> rejectFriendRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable Long requestId) {
//
//        String authId = jwt.getSubject();
//
//        try {
//            friendService.rejectFriendRequest(requestId, authId);
//            return ResponseEntity.ok().build();
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().build();
//        }
//        catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("/request/pending")
//    public ResponseEntity<List<FriendEntity>> getPendingRequests(@AuthenticationPrincipal Jwt jwt) {
//        String authId = jwt.getSubject();
//
//        return ResponseEntity.ok(friendService.getPendingFriendRequests(authId));
//    }
//
//    @GetMapping("/getfriends")
//    public ResponseEntity<List<UserDto>> getFriends(@AuthenticationPrincipal Jwt jwt) {
//        String authId = jwt.getSubject();
//
//        return ResponseEntity.ok(friendService.getFriends(authId));
//    }
//
//    @DeleteMapping("/remove")
//    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, String> request) {
//
//        String authId = jwt.getSubject();
//        String friendEmail = request.get("email");
//
//        try {
//            friendService.removeFriend(authId, friendEmail);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("/check")
//    public ResponseEntity<Map<String, Boolean>> checkFriendship(@AuthenticationPrincipal Jwt jwt, @RequestParam String email) {
//
//        String authId = jwt.getSubject();
//
//        boolean areFriends = friendService.areFriends(authId, email);
//        return ResponseEntity.ok(Map.of("areFriends", areFriends));
//    }
//
//
//}
