package com.roomate.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomate.app.dto.CreateRoomRequest;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Need to explicitly insert the user matching the @WithMockUser username
        // because the service extracts the user from the repository based on SecurityContext.
        UserEntity user = new UserEntity();
        user.setEmail("roome2e@example.com");
        user.setFirstName("Room");
        user.setLastName("Tester");
        user.setPassword("hunter2");
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "roome2e@example.com")
    void testCreateRoomEndpoint() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("E2E Test Room");
        request.setAddress("123 Mocking Blvd");
        request.setDescription("E2E Testing Room");

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("E2E Test Room"))
                .andExpect(jsonPath("$.headRoommateId").value("roome2e@example.com"))
                .andExpect(jsonPath("$.roomCode").exists());
    }

    @Test
    @WithMockUser(username = "roome2e@example.com")
    void testGetRoomsEndpoint() throws Exception {
        // First create a room so we can retrieve it
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("E2E Retrieve Room");
        request.setAddress("404 HTTP Ave");
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then retrieve rooms
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("E2E Retrieve Room"));
    }
}
