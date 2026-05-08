package com.roomate.app.repository;

import com.roomate.app.entities.UtilityEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilityRepository extends JpaRepository<UtilityEntity, Long> {
    @Query("SELECT u FROM UtilityEntity u JOIN FETCH u.room WHERE u.room.id = :roomId")
    List<UtilityEntity> findByRoomId(@Param("roomId") UUID roomId);

    Boolean existsById(UUID utilityId);

    @Query("SELECT u FROM UtilityEntity u JOIN FETCH u.room WHERE u.room.id = :roomId AND u.assignedToMember.id = :roomMemberId")
    List<UtilityEntity> findByRoomIdAndMemberId(@Param("roomId") UUID roomId, @Param("roomMemberId") UUID memberId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UtilityEntity u WHERE u.assignedToMember.id = :roomMemberId")
    void deleteAllByRoomMemberId(@Param("roomMemberId") UUID roomMemberId);

    @Modifying
    @Transactional
    void deleteById(UUID utilityId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UtilityEntity m WHERE m.room.id = :roomId")
    void deleteAllByRoomId(@Param("roomId") UUID roomId);

    @Query("SELECT u FROM UtilityEntity u JOIN FETCH u.room r LEFT JOIN FETCH u.assignedToMember m WHERE u.assignedToMember.id IN :roomMemberIds")
    List<UtilityEntity> findAllByRoomMemberIds(@Param("roomMemberIds") List<UUID> roomMemberIds);

    @Query("SELECT u FROM UtilityEntity u JOIN FETCH u.room r LEFT JOIN FETCH u.assignedToMember m JOIN m.user usr WHERE usr.email = :email")
    List<UtilityEntity> findAllByUserEmail(@Param("email") String email);

    @Query("SELECT u FROM UtilityEntity u LEFT JOIN FETCH u.assignedToMember m LEFT JOIN FETCH m.user WHERE u.id = :utilityId")
    java.util.Optional<UtilityEntity> findByUtilityId(@Param("utilityId") UUID utilityId);
}
