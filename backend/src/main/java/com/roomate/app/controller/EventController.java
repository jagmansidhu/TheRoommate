package com.roomate.app.controller;

import com.roomate.app.dto.EventDTOS.EventDto;
import com.roomate.app.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @GetMapping("/user")
    public ResponseEntity<List<EventDto>> getAllEventsForUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<EventDto> events = eventService.getAllEventsForUser(email);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/user/own")
    public ResponseEntity<List<EventDto>> getUserOwnEvents(@AuthenticationPrincipal  UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<EventDto> events = eventService.getAllEventsForUser(email);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<EventDto>> getEventsForRoom(@PathVariable UUID roomId, @AuthenticationPrincipal  UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<EventDto> events = eventService.getEventsForUserRoom(roomId, email);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/room/{roomId}")
    public ResponseEntity<Void> createEvent(@PathVariable UUID roomId, @RequestBody @Valid EventDto eventDto, @AuthenticationPrincipal  UserDetails userDetails) {
        String email = userDetails.getUsername();
        eventService.createEventForRoom(eventDto, roomId, email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Void> updateEvent(@PathVariable UUID eventId, @RequestBody EventDto eventDto, @AuthenticationPrincipal  UserDetails userDetails) {
        String email = userDetails.getUsername();
        eventService.updateEvent(eventDto, eventId, email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId, @AuthenticationPrincipal  UserDetails userDetails) {
        String email = userDetails.getUsername();
        eventService.deleteEvent(eventId, email);
        return ResponseEntity.ok().build();
    }
}
