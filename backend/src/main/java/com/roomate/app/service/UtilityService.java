package com.roomate.app.service;

import com.roomate.app.dto.UtilityCreateDto;
import com.roomate.app.dto.UtilityDto;
import com.roomate.app.entities.UtilityEntity;

import java.util.List;
import java.util.UUID;

public interface UtilityService {
    List<UtilityEntity> createUtility(UtilityCreateDto dto);

    List<UtilityDto> getUtilitiesByRoom(UUID roomId);

    void updateUtilitiesOnUserChange(UUID roomId);

    List<UtilityDto> getUtilitiesByRoomandMemberId(UUID roomId, UUID memberId);

    List<UtilityDto> getUpcomingUtilities(String id);

    void deleteUtility(UUID utilityId);
}
