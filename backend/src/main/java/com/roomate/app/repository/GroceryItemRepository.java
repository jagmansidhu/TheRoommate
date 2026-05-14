package com.roomate.app.repository;

import com.roomate.app.entities.grocery.GroceryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroceryItemRepository extends JpaRepository<GroceryItemEntity, UUID> {

    /**
     * Find all items in a grocery list
     */
    List<GroceryItemEntity> findByGroceryListIdOrderByCreatedAtDesc(UUID groceryListId);

    /**
     * Find unpurchased items in a list
     */
    List<GroceryItemEntity> findByGroceryListIdAndIsPurchasedFalse(UUID groceryListId);

    /**
     * Find purchased items in a list
     */
    List<GroceryItemEntity> findByGroceryListIdAndIsPurchasedTrue(UUID groceryListId);

    /**
     * Find items by category in a list
     */
    List<GroceryItemEntity> findByGroceryListIdAndCategoryOrderByCreatedAtDesc(UUID groceryListId, String category);

    /**
     * Find items added by a specific member
     */
    List<GroceryItemEntity> findByAddedById(UUID memberId);

    /**
     * Count unpurchased items in a list
     */
    long countByGroceryListIdAndIsPurchasedFalse(UUID groceryListId);

    /**
     * Get distinct categories in a list
     */
    @Query("SELECT DISTINCT g.category FROM GroceryItemEntity g WHERE g.groceryList.id = :listId AND g.category IS NOT NULL")
    List<String> findDistinctCategoriesByListId(@Param("listId") UUID listId);
}
