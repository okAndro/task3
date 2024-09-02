package com.itm.space.backendresources.service;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @MockBean
    private Keycloak keycloakClient;

    @Test
    void shouldCreateUserSuccessfully() {
        // Создаем UserRequest с тестовыми данными
        UserRequest userRequest = new UserRequest(
                "test2",
                "test2@test.com",
                "password",
                "Test2",
                "Test2"
        );

        // Мокаем цепочку вызовов Keycloak API
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        Response mockResponse = mock(Response.class);

        Response.StatusType mockStatusType = Response.Status.CREATED;
        when(mockResponse.getStatusInfo()).thenReturn(mockStatusType);


        when(keycloakClient.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(201); // Успешное создание
        when(mockResponse.getLocation()).thenReturn(java.net.URI.create("http://localhost:8080/auth/admin/realms/realm/users/12345"));

        // Запускаем тестируемый метод
        assertDoesNotThrow(() -> userService.createUser(userRequest));

        // Проверяем, что метод был вызван один раз с любыми аргументами
        verify(keycloakClient.realm(anyString()).users(), times(1)).create(any(UserRepresentation.class));
    }

    @Test
    void shouldHandleKeycloakError() {
        UserRequest userRequest = new UserRequest(
                "johndoe",
                "john.doe@example.com",
                "password",
                "John",
                "Doe"
        );

        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        Response mockResponse = mock(Response.class);

        Response.StatusType mockStatusType = Response.Status.INTERNAL_SERVER_ERROR;
        when(mockResponse.getStatusInfo()).thenReturn(mockStatusType);

        when(keycloakClient.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(500);

        BackendResourcesException exception = assertThrows(BackendResourcesException.class,
                () -> userService.createUser(userRequest));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId.toString());
        userRepresentation.setFirstName("John");
        userRepresentation.setLastName("Doe");
        userRepresentation.setEmail("john.doe@example.com");

        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        RoleMappingResource mockRoleMappingResource = mock(RoleMappingResource.class);
        RoleScopeResource mockRoleScopeResource = mock(RoleScopeResource.class);

        when(keycloakClient.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId.toString())).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUserResource.roles()).thenReturn(mockRoleMappingResource);
        MappingsRepresentation mockMappingsRepresentation = mock(MappingsRepresentation.class);
        when(mockRoleMappingResource.getAll()).thenReturn(mockMappingsRepresentation);
        when(mockMappingsRepresentation.getRealmMappings()).thenReturn(Collections.emptyList());
        when(mockUserResource.groups()).thenReturn(Collections.emptyList());
        UserResponse userResponse = userService.getUserById(userId);

        assertNotNull(userResponse);
        assertEquals("John", userResponse.getFirstName());
        assertEquals("Doe", userResponse.getLastName());
        assertEquals("john.doe@example.com", userResponse.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        when(keycloakClient.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId.toString())).thenThrow(new NotFoundException("User not found"));

        assertThrows(BackendResourcesException.class, () -> userService.getUserById(userId));
    }
}