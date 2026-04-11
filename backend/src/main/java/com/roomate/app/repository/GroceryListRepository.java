package com.roomate.app.repository;

import com.roomate.app.entities.grocery.GroceryListEntity;
import com.roomate.app.entities.grocery.GroceryListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroceryListRepository extends JpaRepository<GroceryListEntity, UUID> {

    /**
     * Find all grocery lists for a room, ordered by creation date (newest first)
     */
    List<GroceryListEntity> findByRoomIdOrderByCreatedAtDesc(UUID roomId);

    /**
     * Find active grocery lists for a room
     */
    List<GroceryListEntity> findByRoomIdAndStatus(UUID roomId, GroceryListStatus status);

    /**
     * Find all non-archived lists for a room
     */
    @Query("SELECT g FROM GroceryListEntity g WHERE g.room.id = :roomId AND g.status != 'ARCHIVED' ORDER BY g.createdAt DESC")
    List<GroceryListEntity> findActiveByRoomId(@Param("roomId") UUID roomId);

    /**
     * Count active lists for a room
     */
    long countByRoomIdAndStatus(UUID roomId, GroceryListStatus status);

    @Modifying
    @Query("DELETE FROM GroceryListEntity g WHERE g.room.id = :roomId")
    void deleteAllByRoomId(@Param("roomId") UUID roomId);
}
