package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "admin", roles = {"MODERATOR"})
    void shouldReturnUserById() throws Exception {
        UUID userId = UUID.fromString("b6a5d8e0-d48d-11eb-b8bc-0242ac130003");
        UserResponse userResponse = new UserResponse
                ("John", "Doe",
                        "john.doe@example.com", List.of("MODERATOR"), List.of());

        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("MODERATOR"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MODERATOR"})
    void shouldCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest(
                "testuser",
                "user@test.com",
                "password",
                "Test",
                "User"
        );

        String jsonRequest = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MODERATOR"})
    void shouldReturnHello() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/hello"))
                .andExpect(status().isOk()) // Ожидаем, что статус ответа будет 200 (OK)
                .andExpect(content().string("admin")); // Проверяем, что ответ содержит имя пользователя
    }

    @Test
    @WithMockUser(username = "user", roles = {"NO ROLES"})
    void shouldReturn403ForUnauthorizedAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/hello"))
                .andExpect(status().isForbidden()); // Ожидаем, что статус ответа будет 403 (FORBIDDEN)
    }

    @Test
    void shouldReturn401ForNoAutentificationAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/hello"))
                .andExpect(status().isUnauthorized()); // Ожидаем, что статус ответа будет 401 (UNAUTHORIZED)
    }
}