package com.roomate.app.controller;

import com.roomate.app.dto.BudgetEntryDto;
import com.roomate.app.dto.BudgetSettingsDto;
import com.roomate.app.dto.BudgetStatsDto;
import com.roomate.app.dto.CreateBudgetEntryRequest;
import com.roomate.app.dto.UpdateCategoryRequest;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/entries")
    public ResponseEntity<List<BudgetEntryDto>> getEntries(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            List<BudgetEntryDto> entries;
            if (year != null && month != null) {
                entries = budgetService.getEntriesByMonth(email, year, month);
            } else {
                entries = budgetService.getEntries(email);
            }
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<BudgetStatsDto> getStats(
            @RequestParam(required = false, defaultValue = "0") int year,
            @RequestParam(required = false, defaultValue = "0") int month,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            return ResponseEntity.ok(budgetService.getStats(email, year, month));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/entries")
    public ResponseEntity<BudgetEntryDto> createEntry(
            @RequestBody CreateBudgetEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            return ResponseEntity.ok(budgetService.createEntry(email, request));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/entries/{id}/category")
    public ResponseEntity<Void> updateCategory(
            @PathVariable UUID id,
            @RequestBody UpdateCategoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            budgetService.updateEntryCategory(id, email, request.category);
            return ResponseEntity.ok().build();
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            budgetService.deleteEntry(id, email);
            return ResponseEntity.ok().build();
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<BudgetSettingsDto> getSettings(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            return ResponseEntity.ok(budgetService.getBudgetSettings(email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<Void> updateSettings(
            @RequestBody BudgetSettingsDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            budgetService.setBudgetSettings(email, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
