package com.roomate.app.controller;

import com.roomate.app.dto.ledger.*;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing shared ledger entries.
 * Handles expense creation, split management, and payment tracking.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    /**
     * Create a new ledger entry (rent, utility, etc.)
     * Only landlords and head roommates can create entries.
     */
    @PostMapping("/rooms/{roomId}/ledger")
    public ResponseEntity<LedgerEntryDto> createLedgerEntry(
            @PathVariable UUID roomId,
            @Valid @RequestBody LedgerEntryCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            dto.setRoomId(roomId);
            LedgerEntryDto created = ledgerService.createLedgerEntry(dto, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all ledger entries for a room
     */
    @GetMapping("/rooms/{roomId}/ledger")
    public ResponseEntity<List<LedgerEntryDto>> getRoomLedgerEntries(
            @PathVariable UUID roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<LedgerEntryDto> entries = ledgerService.getLedgerEntriesForRoom(roomId, userDetails.getUsername());
            return ResponseEntity.ok(entries);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a specific ledger entry by ID
     */
    @GetMapping("/ledger/{entryId}")
    public ResponseEntity<LedgerEntryDto> getLedgerEntry(
            @PathVariable UUID entryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            LedgerEntryDto entry = ledgerService.getLedgerEntryById(entryId, userDetails.getUsername());
            return ResponseEntity.ok(entry);
        } catch (UserApiError e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Assign custom splits to a ledger entry (head roommate only)
     */
    @PutMapping("/ledger/{entryId}/splits")
    public ResponseEntity<LedgerEntryDto> assignSplits(
            @PathVariable UUID entryId,
            @Valid @RequestBody AssignSplitsDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            LedgerEntryDto updated = ledgerService.assignSplits(entryId, dto, userDetails.getUsername());
            return ResponseEntity.ok(updated);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Auto-calculate equal splits for a ledger entry
     */
    @PostMapping("/ledger/{entryId}/splits/equal")
    public ResponseEntity<LedgerEntryDto> calculateEqualSplits(
            @PathVariable UUID entryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            LedgerEntryDto updated = ledgerService.calculateEqualSplits(entryId, userDetails.getUsername());
            return ResponseEntity.ok(updated);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Record a payment against a split
     */
    @PostMapping("/ledger/splits/{splitId}/pay")
    public ResponseEntity<LedgerSplitDto> recordPayment(
            @PathVariable UUID splitId,
            @Valid @RequestBody RecordPaymentDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            LedgerSplitDto updated = ledgerService.recordPayment(splitId, dto, userDetails.getUsername());
            return ResponseEntity.ok(updated);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all member balances for a room
     */
    @GetMapping("/rooms/{roomId}/ledger/balances")
    public ResponseEntity<List<MemberBalanceDto>> getMemberBalances(
            @PathVariable UUID roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<MemberBalanceDto> balances = ledgerService.getMemberBalances(roomId, userDetails.getUsername());
            return ResponseEntity.ok(balances);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a specific member's balance
     */
    @GetMapping("/rooms/{roomId}/ledger/balances/{memberId}")
    public ResponseEntity<MemberBalanceDto> getMemberBalance(
            @PathVariable UUID roomId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            MemberBalanceDto balance = ledgerService.getMemberBalance(roomId, memberId, userDetails.getUsername());
            return ResponseEntity.ok(balance);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cancel a ledger entry
     */
    @PostMapping("/ledger/{entryId}/cancel")
    public ResponseEntity<Void> cancelLedgerEntry(
            @PathVariable UUID entryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ledgerService.cancelLedgerEntry(entryId, userDetails.getUsername());
            return ResponseEntity.ok().build();
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a ledger entry (head roommate only)
     */
    @DeleteMapping("/ledger/{entryId}")
    public ResponseEntity<Void> deleteLedgerEntry(
            @PathVariable UUID entryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ledgerService.deleteLedgerEntry(entryId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
