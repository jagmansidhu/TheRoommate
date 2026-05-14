package com.roomate.app.service;

import com.roomate.app.dto.EventDTOS.EventDto;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventDto> getAllEventsForUser(String email);
    List<EventDto> getEventsForUserRoom(UUID roomID, String email);
    void createEventForRoom(EventDto eventDto, UUID roomid, String email);
    void updateEvent(EventDto eventDto, UUID eventID, String email);
    void deleteEvent(UUID eventId, String email);
}
