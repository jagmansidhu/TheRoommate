package com.roomate.app.controller;

import com.roomate.app.dto.ChoreCreateDto;
import com.roomate.app.dto.ChoreDto;
import com.roomate.app.dto.CompletionUpdateDto;
import com.roomate.app.service.ChoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chores")
public class ChoreController {
    private final ChoreService choreService;

    @PostMapping("/room/{roomId}")
    public ResponseEntity<List<ChoreDto>> createChores(@PathVariable UUID roomId,
            @RequestBody List<ChoreCreateDto> choreDTOs) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ChoreDto> allChores = choreDTOs.stream()
                .flatMap(dto -> choreService.distributeChores(roomId, dto, email).stream()).collect(Collectors.toList());

        return ResponseEntity.ok(allChores);
    }

    @PostMapping("/room/{roomId}/redistribute")
    public ResponseEntity<Void> redistributeChores(@PathVariable UUID roomId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        choreService.redistributeChores(roomId, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<List<ChoreDto>> getRoomChores(@PathVariable UUID roomId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        choreService.validateRoomMembership(roomId, email);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                .body(choreService.getChoresByRoomId(roomId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ChoreDto>> getUpcomingChores() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ChoreDto> chores = choreService.getChoresByUserId(email);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                .body(chores);
    }

    @GetMapping("/user/me")
    public ResponseEntity<List<ChoreDto>> getMyChores() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(choreService.getChoresByUserId(email));
    }

    @DeleteMapping("/{choreId}")
    public ResponseEntity<Void> deleteChore(@PathVariable UUID choreId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        choreService.deleteChore(choreId, email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{choreId}/completion")
    public ResponseEntity<ChoreDto> updateCompletion(@PathVariable UUID choreId,
            @RequestBody CompletionUpdateDto request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return ResponseEntity.ok(choreService.updateCompletion(choreId, email, request.isCompleted()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/room/{roomId}/type/{choreName}")
    public ResponseEntity<Void> deleteChoresByType(@PathVariable UUID roomId, @PathVariable String choreName) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        choreService.deleteChoresByType(roomId, choreName, email);
        return ResponseEntity.noContent().build();
    }
}
