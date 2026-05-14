package com.roomate.app.service.implementation;

import com.roomate.app.dto.EventDTOS.EventDto;
import com.roomate.app.dto.EventDTOS.EventRoomDto;
import com.roomate.app.dto.EventDTOS.EventUserDto;
import com.roomate.app.entities.EventEntity;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.exceptions.EventAPIException;
import com.roomate.app.repository.EventRepository;
import com.roomate.app.repository.RoomRepository;
import com.roomate.app.repository.UserRepository;
import com.roomate.app.service.EventService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceimplt implements EventService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;

    public EventServiceimplt(EventRepository eventRepository, UserRepository userRepository, RoomRepository roomRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public List<EventDto> getAllEventsForUser(String email) {
        List<EventEntity> events = eventRepository.getAllEventsForUserRooms(email);

        return events.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<EventDto> getEventsForUserRoom(UUID roomID, String email) {
        List<EventEntity> events = eventRepository.getAllEventsForUserRoom(roomID, email);
        return events.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createEventForRoom(EventDto eventDto,UUID roomid, String email) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTitle(eventDto.getTitle());
        eventEntity.setDescription(eventDto.getDescription());
        eventEntity.setStartTime(eventDto.getStartTime());
        eventEntity.setEndTime(eventDto.getEndTime());
        eventEntity.setRoom(roomRepository.getRoomEntityById(roomid).orElse(null));
        eventEntity.setUser(userRepository.getUserByEmail(email));
        eventEntity.setCreated(LocalDateTime.now());
        eventEntity.setUpdated(null);
        eventRepository.save(eventEntity);
    }

    @Override
    @Transactional
    public void updateEvent(EventDto eventDto, UUID eventID, String email) {
        EventEntity eventEntity = eventRepository.getEventById(email, eventID);

        eventExceptions(eventEntity == null, eventEntity, email);

        eventEntity.setTitle(eventDto.getTitle());
        eventEntity.setDescription(eventDto.getDescription());
        eventEntity.setStartTime(eventDto.getStartTime());
        eventEntity.setEndTime(eventDto.getEndTime());
        eventEntity.setUpdated(LocalDateTime.now());
        
        eventRepository.save(eventEntity);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID eventId, String email) {
        EventEntity eventEntity = eventRepository.getEventById(email, eventId);

        eventExceptions(eventId == null, eventEntity, email);
        eventRepository.deleteEventById(email,eventId);
    }

    private EventDto convertToDto(EventEntity eventEntity) {
        EventDto eventDto = new EventDto();
        eventDto.setId(eventEntity.getId());
        eventDto.setTitle(eventEntity.getTitle());
        eventDto.setDescription(eventEntity.getDescription());
        eventDto.setStartTime(eventEntity.getStartTime());
        eventDto.setEndTime(eventEntity.getEndTime());

        RoomEntity roomEntity = eventEntity.getRoom();
        EventRoomDto roomDto = new EventRoomDto(
            roomEntity.getRoomCode(),
            roomEntity.getName()
        );
        eventDto.setRooms(roomDto);

        UserEntity userEntity = eventEntity.getUser();
        EventUserDto userDto = new EventUserDto(
            userEntity.getFirstName(),
            userEntity.getLastName(),
            userEntity.getEmail()
        );
        eventDto.setUser(userDto);

        return eventDto;
    }

    private static void eventExceptions(boolean eventEntity, EventEntity eventEntity1, String email) {
        if (eventEntity) {
            throw new EventAPIException("Event not found");
        }

        if (!eventEntity1.getUser().getEmail().equals(email)) {
            throw new EventAPIException("Wrong auth id");
        }
    }
}
