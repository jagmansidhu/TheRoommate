package com.roomate.app.service;

import com.roomate.app.dto.RegisterDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRegisterAndRetrieveUser() {
        // Arrange
        RegisterDto dto = new RegisterDto();
        dto.setEmail("integration@example.com");
        dto.setFirstName("Int");
        dto.setLastName("Test");
        dto.setPassword("SecretPass123!");

        // Act
        String token = userService.registerUser(dto);

        // Assert
        assertNotNull(token);
        
        // Retrieve directly from DB to verify persistence and JPA mappings
        UserEntity savedUser = userRepository.findByEmail("integration@example.com").orElse(null);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("Int", savedUser.getFirstName());
        assertEquals("Test", savedUser.getLastName());
        
        // Assert password was encoded and isn't plain text
        assertNotEquals("SecretPass123!", savedUser.getPassword());
        assertTrue(savedUser.getPassword().length() > 20); // Bcrypt hashes are long
    }

    @Test
    void testRegisterDuplicateUserThrowsException() {
        // Arrange
        RegisterDto dto = new RegisterDto();
        dto.setEmail("duplicate@example.com");
        dto.setFirstName("Int");
        dto.setLastName("Test");
        dto.setPassword("SecretPass123!");

        // Act: Save first time
        userService.registerUser(dto);

        // Assert: Save second time throws DuplicateKeyException
        assertThrows(DuplicateKeyException.class, () -> userService.registerUser(dto));
    }
}
