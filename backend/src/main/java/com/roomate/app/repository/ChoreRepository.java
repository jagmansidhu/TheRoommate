package com.roomate.app.repository;

import com.roomate.app.entities.ChoreEntity;
import com.roomate.app.entities.room.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChoreRepository extends JpaRepository<ChoreEntity, Long> {
    List<ChoreEntity> findByRoomAndDueAtAfter(RoomEntity room, LocalDateTime date);

    @Query("SELECT c FROM ChoreEntity c LEFT JOIN FETCH c.assignedToMember m LEFT JOIN FETCH m.user WHERE c.room = :room")
    List<ChoreEntity> findByRoom(@Param("room") RoomEntity room);

    void deleteById(UUID choreId);

    @Query("SELECT c FROM ChoreEntity c " +
            "LEFT JOIN FETCH c.assignedToMember m " +
            "LEFT JOIN FETCH m.user " +
            "WHERE c.room = :room")
    List<ChoreEntity> findByRoomWithMemberAndUser(@Param("room") RoomEntity room);

    void deleteAllByRoomIdAndChoreName(UUID roomId, String choreName);

    @Query("SELECT c FROM ChoreEntity c JOIN FETCH c.room r LEFT JOIN FETCH c.assignedToMember m LEFT JOIN FETCH m.user WHERE c.assignedToMember.id IN :roomMemberIds")
    List<ChoreEntity> findAllByRoomMemberIds(@Param("roomMemberIds") List<UUID> roomMemberIds);

    @Query("SELECT c FROM ChoreEntity c LEFT JOIN FETCH c.assignedToMember m LEFT JOIN FETCH m.user WHERE c.id = :choreId")
    java.util.Optional<ChoreEntity> findByChoreId(@Param("choreId") UUID choreId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChoreEntity m WHERE m.room.id = :roomId")
    void deleteAllByRoomId(@Param("roomId") UUID roomId);
}
