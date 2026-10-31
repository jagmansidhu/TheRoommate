package com.roomate.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomate.app.dto.LoginDto;
import com.roomate.app.dto.RegisterDto;
import com.roomate.app.dto.UserDTOS.UpdateProfileDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void statusResponseSetsCsrfCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/user/status").header("Origin", "http://localhost:3000"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(header().exists("X-CSRF-TOKEN"))
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("X-CSRF-TOKEN")))
                .andReturn();
        assertEquals(result.getResponse().getCookie("XSRF-TOKEN").getValue(),
                result.getResponse().getHeader("X-CSRF-TOKEN"));
    }

    @Test
    void cookiesFollowFirstForwardedScheme() throws Exception {
        assertCsrfCookie("https,http", false, true, "None");
        assertCsrfCookie(" HTTPS , http", false, true, "None");
        assertCsrfCookie("http,https", false, false, "Lax");
        assertCsrfCookie("nothttps", false, false, "Lax");
        assertCsrfCookie(null, true, true, "None");

        String email = "forwarded-proto@example.com";
        RegisterDto register = new RegisterDto();
        register.setEmail(email);
        register.setFirstName("Forward");
        register.setLastName("Proto");
        register.setPassword("strongPassword123!");
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        assertJwtCookie(email, "https,http", true, "None");
        assertJwtCookie(email, "http,https", false, "Lax");
    }

    @Test
    void testRegisterUserEndpoint() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("e2e@example.com");
        dto.setFirstName("E2E");
        dto.setLastName("User");
        dto.setPassword("strongPassword123!");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testRegisterDuplicateUserEndpoint() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("duplicatee2e@example.com");
        dto.setFirstName("E2E");
        dto.setLastName("User");
        dto.setPassword("strongPassword123!");

        // First registration succeeds
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Second registration fails with 409 Conflict
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with this email already exists."));
    }

    @Test
    @WithMockUser(username = "profilejson@example.com")
    void profileJsonHasNoPassword() throws Exception {
        String email = "profilejson@example.com";
        String submittedPassword = "NewSecret9!pass";

        RegisterDto register = new RegisterDto();
        register.setEmail(email);
        register.setFirstName("Profile");
        register.setLastName("Json");
        register.setPassword("strongPassword123!");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        UpdateProfileDto update = new UpdateProfileDto();
        update.setPassword(submittedPassword);

        assertProfileOmitsPassword(put("/user/updateProfile"), update, submittedPassword);
        assertProfileOmitsPassword(put("/api/profile/update_profile"), update, submittedPassword);
    }

    private void assertProfileOmitsPassword(MockHttpServletRequestBuilder request,
                                            UpdateProfileDto body,
                                            String submittedPassword) throws Exception {
        mockMvc.perform(withCsrf(request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(content().string(not(containsString(submittedPassword))))
                .andExpect(jsonPath("$.email").exists());
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) throws Exception {
        Cookie token = mockMvc.perform(get("/user/status")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        return request.cookie(token).header("X-CSRF-TOKEN", token.getValue());
    }

    private void assertCsrfCookie(String forwardedProto, boolean secureRequest, boolean expectSecure,
                                   String expectSameSite) throws Exception {
        MockHttpServletRequestBuilder request = get("/user/status").secure(secureRequest);
        if (forwardedProto != null) {
            request.header("X-Forwarded-Proto", forwardedProto);
        }
        Cookie cookie = mockMvc.perform(request).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        assertEquals(expectSecure, cookie.getSecure());
        assertEquals(expectSameSite, cookie.getAttribute("SameSite"));
    }

    private void assertJwtCookie(String email, String forwardedProto, boolean expectSecure, String expectSameSite)
            throws Exception {
        LoginDto login = new LoginDto(email, "strongPassword123!");
        Cookie jwt = mockMvc.perform(post("/user/login")
                        .header("X-Forwarded-Proto", forwardedProto)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("jwt");
        assertNotNull(jwt);
        assertEquals(expectSecure, jwt.getSecure());
        assertEquals(expectSameSite, jwt.getAttribute("SameSite"));
    }
}
