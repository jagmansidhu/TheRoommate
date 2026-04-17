package com.roomate.app.repository;

import com.roomate.app.entities.room.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {

    @Query("SELECT DISTINCT r FROM RoomEntity r JOIN FETCH r.members m JOIN FETCH m.user WHERE r.id IN (SELECT r2.id FROM RoomEntity r2 JOIN r2.members m2 WHERE m2.user.id = :userId)")
    List<RoomEntity> findByMemberUserId(@Param("userId") Long userId);

    Optional<RoomEntity> findByRoomCode(String roomCode);

    boolean existsByRoomCode(String roomCode);

    RoomEntity getRoomEntityByRoomCode(String roomCode);

    Optional<RoomEntity> getRoomEntityById(UUID id);


    @Query("SELECT COUNT(r) FROM RoomEntity r JOIN r.members m WHERE m.user.id = :id")
    int countRoomsByUserId(Long id);
}