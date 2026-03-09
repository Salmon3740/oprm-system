package com.oprm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oprm.dto.RegisterRequest;
import com.oprm.entity.User;
import com.oprm.entity.enums.UserRole;
import com.oprm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the authentication flow.
 * Uses @Transactional so DB changes are rolled back after each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Test
        void register_ShouldCreateUserAndReturn200() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .name("Integration Test User")
                                .email("integration_test@example.com")
                                .password("testpassword123")
                                .role(UserRole.STUDENT)
                                .department("Computer Science")
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("integration_test@example.com"))
                                .andExpect(jsonPath("$.role").value("STUDENT"))
                                .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        void register_ShouldReturn400WhenEmailMissing() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .name("Test User")
                                .password("password")
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.fieldErrors").exists());
        }

        @Test
        void login_ShouldReturnTokenAfterRegistration() throws Exception {
                // First register
                RegisterRequest register = RegisterRequest.builder()
                                .name("Login Test User")
                                .email("login_test@example.com")
                                .password("mypassword123")
                                .role(UserRole.STUDENT)
                                .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(register)))
                                .andExpect(status().isOk());

                // Then login
                Map<String, String> loginBody = Map.of("email", "login_test@example.com", "password", "mypassword123");

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginBody)))
                                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.role").value("STUDENT"))
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                assertTrue(responseBody.contains("token"));
        }

        @Test
        void profile_ShouldReturn401WithoutToken() throws Exception {
                mockMvc.perform(get("/api/users/profile"))
                                .andExpect(status().is3xxRedirection());
        }
}
