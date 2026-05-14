package com.roomate.app.service;

import com.roomate.app.dto.grocery.*;

import java.util.List;
import java.util.UUID;

public interface GroceryService {

    GroceryListDto createGroceryList(GroceryListCreateDto dto, String userEmail);

    List<GroceryListDto> getGroceryListsForRoom(UUID roomId, String userEmail);

    List<GroceryListDto> getActiveGroceryListsForRoom(UUID roomId, String userEmail);

    GroceryListDto getGroceryListById(UUID listId, String userEmail);

    GroceryItemDto addItem(UUID listId, GroceryItemCreateDto dto, String userEmail);

    GroceryItemDto updateItem(UUID itemId, GroceryItemCreateDto dto, String userEmail);

    GroceryItemDto markItemPurchased(UUID itemId, PurchaseItemDto dto, String userEmail);

    GroceryItemDto unmarkItemPurchased(UUID itemId, String userEmail);

    void removeItem(UUID itemId, String userEmail);

    GroceryListDto completeList(UUID listId, String userEmail);

    GroceryListDto archiveList(UUID listId, String userEmail);

    void deleteList(UUID listId, String userEmail);
}
