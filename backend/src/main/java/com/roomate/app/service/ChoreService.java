package com.roomate.app.service;

import com.roomate.app.dto.ChoreCreateDto;
import com.roomate.app.dto.ChoreDto;

import java.util.List;
import java.util.UUID;

public interface ChoreService {
    List<ChoreDto> distributeChores(UUID roomId, ChoreCreateDto choreDTO, String userEmail);

    void redistributeChores(UUID roomId, String userEmail);

    List<ChoreDto> getChoresByRoomId(UUID roomId);

    void validateRoomMembership(UUID roomId, String userEmail);

    void deleteChore(UUID choreId, String userEmail);

    void deleteChoresByType(UUID roomId, String choreName, String userEmail);

    List<ChoreDto> getChoresByUserId(String id);

    ChoreDto updateCompletion(UUID choreId, String userEmail, boolean completed);
}
