package com.roomate.app.repository;

import com.roomate.app.entities.VerificationTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, Long> {
    Optional<VerificationTokenEntity> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationTokenEntity u WHERE u.user.id = :userid")
    void deleteByUser_Id(@Param("userid") Long userid);

}
