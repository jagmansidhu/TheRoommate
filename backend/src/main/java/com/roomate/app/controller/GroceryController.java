package com.roomate.app.controller;

import com.roomate.app.dto.grocery.*;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.service.GroceryService;
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
 * REST controller for managing shared grocery lists.
 * Handles list creation, item management, and purchase tracking.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GroceryController {

    private final GroceryService groceryService;

    /**
     * Create a new grocery list for a room
     */
    @PostMapping("/rooms/{roomId}/groceries")
    public ResponseEntity<GroceryListDto> createGroceryList(
            @PathVariable UUID roomId,
            @Valid @RequestBody GroceryListCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            dto.setRoomId(roomId);
            GroceryListDto created = groceryService.createGroceryList(dto, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all grocery lists for a room
     */
    @GetMapping("/rooms/{roomId}/groceries")
    public ResponseEntity<List<GroceryListDto>> getRoomGroceryLists(
            @PathVariable UUID roomId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<GroceryListDto> lists;
            if (activeOnly) {
                lists = groceryService.getActiveGroceryListsForRoom(roomId, userDetails.getUsername());
            } else {
                lists = groceryService.getGroceryListsForRoom(roomId, userDetails.getUsername());
            }
            return ResponseEntity.ok(lists);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a specific grocery list by ID (with all items)
     */
    @GetMapping("/groceries/{listId}")
    public ResponseEntity<GroceryListDto> getGroceryList(
            @PathVariable UUID listId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            GroceryListDto list = groceryService.getGroceryListById(listId, userDetails.getUsername());
            return ResponseEntity.ok(list);
        } catch (UserApiError e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add an item to a grocery list
     */
    @PostMapping("/groceries/{listId}/items")
    public ResponseEntity<GroceryItemDto> addItem(
            @PathVariable UUID listId,
            @Valid @RequestBody GroceryItemCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            GroceryItemDto item = groceryService.addItem(listId, dto, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update a grocery item
     */
    @PutMapping("/groceries/items/{itemId}")
    public ResponseEntity<GroceryItemDto> updateItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody GroceryItemCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            GroceryItemDto item = groceryService.updateItem(itemId, dto, userDetails.getUsername());
            return ResponseEntity.ok(item);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Mark an item as purchased
     */
    @PutMapping("/groceries/items/{itemId}/purchase")
    public ResponseEntity<GroceryItemDto> markItemPurchased(
            @PathVariable UUID itemId,
            @RequestBody(required = false) PurchaseItemDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (dto == null) {
                dto = new PurchaseItemDto();
            }
            GroceryItemDto item = groceryService.markItemPurchased(itemId, dto, userDetails.getUsername());
            return ResponseEntity.ok(item);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Unmark an item (revert purchase)
     */
    @DeleteMapping("/groceries/items/{itemId}/purchase")
    public ResponseEntity<GroceryItemDto> unmarkItemPurchased(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            GroceryItemDto item = groceryService.unmarkItemPurchased(itemId, userDetails.getUsername());
            return ResponseEntity.ok(item);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Remove an item from a list
     */
    @DeleteMapping("/groceries/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            groceryService.removeItem(itemId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Complete a grocery list (shopping done)
     */
    @PostMapping("/groceries/{listId}/complete")
    public ResponseEntity<GroceryListDto> completeList(
            @PathVariable UUID listId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            GroceryListDto list = groceryService.completeList(listId, userDetails.getUsername());
            return ResponseEntity.ok(list);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Archive a grocery list (head roommate only)
     */
    @PostMapping("/groceries/{listId}/archive")
    public ResponseEntity<GroceryListDto> archiveList(
            @PathVariable UUID listId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            GroceryListDto list = groceryService.archiveList(listId, userDetails.getUsername());
            return ResponseEntity.ok(list);
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a grocery list
     */
    @DeleteMapping("/groceries/{listId}")
    public ResponseEntity<Void> deleteList(
            @PathVariable UUID listId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            groceryService.deleteList(listId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (UserApiError e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
