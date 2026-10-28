package com.roomate.app.service;

import com.roomate.app.dto.UtilityCreateDto;
import com.roomate.app.dto.UtilityDto;

import java.util.List;
import java.util.UUID;

public interface UtilityService {
    List<UtilityDto> createUtility(UtilityCreateDto dto, String userEmail);

    List<UtilityDto> getUtilitiesByRoom(UUID roomId);

    List<UtilityDto> getUtilitiesByRoomandMemberId(UUID roomId, UUID memberId);

    void validateRoomMembership(UUID roomId, String userEmail);

    List<UtilityDto> getUpcomingUtilities(String id);

    void deleteUtility(UUID utilityId, String userEmail);

    UtilityDto updateCompletion(UUID utilityId, String userEmail, boolean completed);
}
