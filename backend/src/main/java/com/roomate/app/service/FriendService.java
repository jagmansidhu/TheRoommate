//package com.roomate.app.service;
//
//import com.roomate.app.dto.UserDTOS.UserDto;
//import com.roomate.app.entities.friendEntity.FriendEntity;
//
//import java.util.List;
//
//public interface FriendService {
//    FriendEntity sendFriendRequest(String authId, String friendEmail);
//    void acceptFriendRequest(Long requestId, String authId);
//    void rejectFriendRequest(Long requestId, String authId);
//    List<FriendEntity> getPendingFriendRequests(String authId);
//    List<UserDto> getFriends(String authId);
//    void removeFriend(String authId, String friendEmail);
//    boolean areFriends(String authId, String friendEmail);
//}
