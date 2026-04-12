package com.roomate.app.repository;

import com.roomate.app.entities.UtilityEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UtilityRepository extends JpaRepository<UtilityEntity, Long> {
    List<UtilityEntity> findByRoomId(UUID roomId);

    Boolean existsById(UUID utilityId);

    @Query("SELECT u FROM UtilityEntity u WHERE u.room.id = :roomId AND u.assignedToMember.id = :roomMemberId")
    List<UtilityEntity> findByRoomIdAndMemberId(@Param("roomId") UUID roomId, @Param("roomMemberId") UUID memberId);

    @Query("SELECT u FROM UtilityEntity u WHERE u.assignedToMember = :userId")
    List<UtilityEntity> findAllByUserId(@Param("userId") Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM UtilityEntity u WHERE u.assignedToMember.id = :roomMemberId")
    void deleteAllByRoomMemberId(@Param("roomMemberId") UUID roomMemberId);

    @Modifying
    @Transactional
    void deleteById(UUID utilityId);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM UtilityEntity m WHERE m.room.id = :roomId")
    void deleteAllByRoomId(@Param("roomId") UUID roomId);

    @Query("SELECT u FROM UtilityEntity u WHERE u.assignedToMember.id IN :roomMemberIds")
    List<UtilityEntity> findAllByRoomMemberIds(@Param("roomMemberIds") List<UUID> roomMemberIds);
}
