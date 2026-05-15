package com.roomate.app.repository.budget;

import com.roomate.app.entities.budget.UserBudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBudgetRepository extends JpaRepository<UserBudgetEntity, UUID> {

    Optional<UserBudgetEntity> findByUserId(String userId);
}
