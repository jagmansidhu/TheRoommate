package com.roomate.app.controller;

import com.roomate.app.dto.UtilityCreateDto;
import com.roomate.app.dto.UtilityDto;
import com.roomate.app.dto.CompletionUpdateDto;
import com.roomate.app.entities.UtilityEntity;
import com.roomate.app.service.UtilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/utility")
public class UtilityControler {
    private final UtilityService utilityService;

    @PostMapping("/create")
    public ResponseEntity<List<UtilityEntity>> createUtility(@RequestBody UtilityCreateDto dto) {
        List<UtilityEntity> utility = utilityService.createUtility(dto);
        return ResponseEntity.ok(utility);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<List<UtilityDto>> getUtilitiesByRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                .body(utilityService.getUtilitiesByRoom(roomId));
    }

    @GetMapping("/{memberId}/room/{roomId}")
    public ResponseEntity<List<UtilityDto>> getUtilitiesByRoomabdMemberId(@PathVariable UUID roomId,
            @PathVariable UUID memberId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                .body(utilityService.getUtilitiesByRoomandMemberId(roomId, memberId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<UtilityDto>> getUpcomingUtilities(@RequestParam String id) {
        List<UtilityDto> utilities = utilityService.getUpcomingUtilities(id);
        if (utilities.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                .body(utilities);
    }

    // Returns utilities assigned to the currently authenticated user.
    // Email is resolved from the JWT via the Spring Security context — no query
    // param needed.
    @GetMapping("/user/me")
    public ResponseEntity<List<UtilityDto>> getMyUtilities() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<UtilityDto> utilities = utilityService.getUpcomingUtilities(email);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(utilities);
    }

    @DeleteMapping("/{utilityId}")
    public ResponseEntity<Boolean> deleteUtility(@PathVariable UUID utilityId) {
        utilityService.deleteUtility(utilityId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{utilityId}/completion")
    public ResponseEntity<UtilityDto> updateCompletion(@PathVariable UUID utilityId,
            @RequestBody CompletionUpdateDto request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return ResponseEntity.ok(utilityService.updateCompletion(utilityId, email, request.isCompleted()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}