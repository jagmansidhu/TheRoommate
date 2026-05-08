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
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChoreRepository extends JpaRepository<ChoreEntity, Long> {
    @Query("SELECT c FROM ChoreEntity c LEFT JOIN FETCH c.assignedToMember m LEFT JOIN FETCH m.user WHERE c.id = :choreId")
    Optional<ChoreEntity> findByChoreId(@Param("choreId") UUID choreId);

    @Query("SELECT c FROM ChoreEntity c LEFT JOIN FETCH c.assignedToMember m LEFT JOIN FETCH m.user WHERE c.room = :room AND c.dueAt >= :start AND c.dueAt < :end")
    List<ChoreEntity> findByRoomAndDueDateRange(@Param("room") RoomEntity room, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Modifying
    @Transactional
    void deleteById(UUID choreId);

    @Query("SELECT c FROM ChoreEntity c " +
            "LEFT JOIN FETCH c.assignedToMember m " +
            "LEFT JOIN FETCH m.user " +
            "WHERE c.room = :room")
    List<ChoreEntity> findByRoomWithMemberAndUser(@Param("room") RoomEntity room);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChoreEntity c WHERE c.room.id = :roomId AND c.choreName = :choreName")
    void deleteAllByRoomIdAndChoreName(@Param("roomId") UUID roomId, @Param("choreName") String choreName);

    @Query("SELECT c FROM ChoreEntity c JOIN FETCH c.room r LEFT JOIN FETCH c.assignedToMember m LEFT JOIN FETCH m.user usr2 WHERE usr2.email = :email")
    List<ChoreEntity> findAllByUserEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChoreEntity m WHERE m.room.id = :roomId")
    void deleteAllByRoomId(@Param("roomId") UUID roomId);

    @Modifying
    @Transactional
    @Query("UPDATE ChoreEntity c SET c.assignedToMember.id = :memberId WHERE c.id IN :choreIds")
    void bulkUpdateAssignedMember(@Param("choreIds") List<UUID> choreIds, @Param("memberId") UUID memberId);
}
