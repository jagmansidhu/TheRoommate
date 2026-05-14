package com.roomate.app.service;

import com.roomate.app.dto.RegisterDto;
import com.roomate.app.dto.UserDTOS.UpdateProfileDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.repository.UserRepository;
import com.roomate.app.repository.VerificationTokenRepository;
import com.roomate.app.service.implementation.UserServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JWTService jwtService;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private UserServiceImplementation userService;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");
        mockUser.setPassword("encodedPassword");
    }

    @Test
    void testRegisterUser_Success() {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("newuser@example.com");
        dto.setFirstName("New");
        dto.setLastName("User");
        dto.setPassword("rawPassword");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");
        
        UserEntity savedArg = new UserEntity();
        savedArg.setId(2L);
        savedArg.setEmail("newuser@example.com");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedArg);
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("fake-jwt-token");

        String result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("fake-jwt-token", result);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(passwordEncoder, times(1)).encode("rawPassword");
    }

    @Test
    void testRegisterUser_DuplicateKeyException() {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserProfile_Success() {
        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setFirstName("UpdatedFirst");
        updateDto.setLastName("UpdatedLast");
        updateDto.setPhone("1234567890");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        UserEntity result = userService.updateUserProfile("test@example.com", updateDto);

        assertEquals("updatedfirst", result.getFirstName()); // Stored in lower case per impl
        assertEquals("updatedlast", result.getLastName()); // Stored in lower case per impl
        assertEquals("1234567890", result.getPhone());
    }

    @Test
    void testUpdateUserProfile_PasswordChangeFail() {
        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setCurPassword("wrongOldPass");
        updateDto.setPassword("newPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongOldPass", "encodedPassword")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.updateUserProfile("test@example.com", updateDto));
    }
}
