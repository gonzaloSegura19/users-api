package com.chakray.usersapi.controller;

import com.chakray.usersapi.model.Address;
import com.chakray.usersapi.model.User;
import com.chakray.usersapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class) // Carga solo el controlador especificado y sus dependencias web
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // Para simular peticiones HTTP

    @MockBean // Crea un mock del UserService y lo inyecta en el controlador
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON y viceversa

    private User user1;
    private Address address1;

    @BeforeEach
    void setUp() {
        address1 = new Address(1L, "workaddress", "street No. 1", "UK");
        // El usuario que el servicio retornaría al controlador (sin password)
        user1 = new User(123L, "user1@mail.com", "user1", null, "01-01-2024 00:00:00", Arrays.asList(address1));
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(userService.findAllUsers(null)).thenReturn(Arrays.asList(user1));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(123L))
                .andExpect(jsonPath("$[0].email").value("user1@mail.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist()); // La contraseña no debe estar en la respuesta
    }

    @Test
    void testGetAllUsers_SortedByEmail() throws Exception {
        User user2 = new User(124L, "auser@mail.com", "auser", null, "01-01-2024 00:00:00", null);
        List<User> sortedUsers = Arrays.asList(user2, user1);

        when(userService.findAllUsers(eq("email"))).thenReturn(sortedUsers);

        mockMvc.perform(get("/users?sortedBy=email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("auser@mail.com"))
                .andExpect(jsonPath("$[1].email").value("user1@mail.com"));
    }

    @Test
    void testGetUserAddresses_Found() throws Exception {
        when(userService.findAddressesByUserId(123L)).thenReturn(Optional.of(Arrays.asList(address1)));

        mockMvc.perform(get("/users/123/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("workaddress"));
    }

    @Test
    void testGetUserAddresses_NotFound() throws Exception {
        when(userService.findAddressesByUserId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/999/addresses"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateAddress_Success() throws Exception {
        Address updatedAddressDetails = new Address(null, "New Work Name", "New Street", "US");
        Address returnedAddress = new Address(1L, "New Work Name", "New Street", "US");

        when(userService.updateAddress(eq(123L), eq(1L), any(Address.class)))
                .thenReturn(Optional.of(returnedAddress));

        mockMvc.perform(put("/users/123/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAddressDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Work Name"))
                .andExpect(jsonPath("$.street").value("New Street"))
                .andExpect(jsonPath("$.countryCode").value("US"));
    }

    @Test
    void testUpdateAddress_NotFound() throws Exception {
        Address updatedAddressDetails = new Address(null, "New Name", "New Street", "US");

        when(userService.updateAddress(eq(123L), eq(99L), any(Address.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/users/123/addresses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAddressDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser_Success() throws Exception {
        User newUserRequest = new User(null, "new@mail.com", "New User", "rawpass", null, null);
        User createdUserResponse = new User(456L, "new@mail.com", "New User", null, "02-01-2024 12:00:00", null);

        when(userService.createUser(any(User.class))).thenReturn(createdUserResponse);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(456L))
                .andExpect(jsonPath("$.email").value("new@mail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        User updates = new User();
        updates.setName("Updated Name");
        updates.setEmail("user1.new@mail.com");
        User updatedUserResponse = new User(123L, "user1.new@mail.com", "Updated Name", null, "01-01-2024 00:00:00", null);

        when(userService.updateUser(eq(123L), any(User.class))).thenReturn(Optional.of(updatedUserResponse));

        mockMvc.perform(patch("/users/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("user1.new@mail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        User updates = new User();
        updates.setName("Non Existent");

        when(userService.updateUser(eq(999L), any(User.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        when(userService.deleteUser(123L)).thenReturn(true);

        mockMvc.perform(delete("/users/123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        when(userService.deleteUser(999L)).thenReturn(false);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }
}
