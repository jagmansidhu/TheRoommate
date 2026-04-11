package com.roomate.app.repository;

import com.roomate.app.entities.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    @Query("SELECT e FROM EventEntity e WHERE e.room.id IN (SELECT r.id FROM RoomEntity r JOIN r.members m WHERE m.user.email = :email)")
    List<EventEntity> getAllEventsForUserRooms(@Param("email") String email);

    @Query("SELECT u from EventEntity u WHERE u.user.email = :email AND u.room.id = :roomid" )
    List<EventEntity> getAllEventsForUserRoom(@Param("roomid") UUID roomid, @Param("email") String email);

    @Query("SELECT e FROM EventEntity e WHERE e.user.email = :email AND e.id = :id")
    EventEntity getEventById(@Param("email") String email, @Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM EventEntity e WHERE e.user.email = :email AND e.id = :id")
    void deleteEventById(@Param("email") String email, @Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM EventEntity e WHERE e.room.id = :roomid")
    void deleteAllByRoomId(@Param("roomid") UUID roomid);
}
