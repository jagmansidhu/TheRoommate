package com.roomate.app.repository;

import com.roomate.app.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    UserEntity getUserByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    Optional<UserEntity> findByEmail(String email);

}
