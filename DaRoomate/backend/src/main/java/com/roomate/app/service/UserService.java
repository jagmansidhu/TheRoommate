package com.roomate.app.service;

import com.roomate.app.dto.RegisterDto;
import com.roomate.app.dto.UserDTOS.UpdateProfileDto;
import com.roomate.app.entities.UserEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DuplicateKeyException;

public interface UserService {

    String createToken(UserEntity savedUser);

    void sendVerificationEmail(@NotNull String email, String token);

    boolean verifyToken(String token);

    UserEntity updateUserProfile(String email, UpdateProfileDto updatedDetails);

    UserEntity getUserEntityByEmail(String email);

    boolean isProfileCompleteInDatabase(String email);

    boolean userExists(String email);

    String registerUser(RegisterDto req) throws DuplicateKeyException;
}
