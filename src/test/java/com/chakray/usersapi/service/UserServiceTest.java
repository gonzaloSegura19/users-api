package com.chakray.usersapi.service;

import com.chakray.usersapi.model.Address;
import com.chakray.usersapi.model.User;
import com.chakray.usersapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock // Crea una instancia simulada (mock) de UserRepository
    private UserRepository userRepository;

    @InjectMocks // Inyecta los mocks (userRepository) en la instancia de UserService
    private UserService userService;

    private User user1WithPass;
    private User user2WithPass;
    private User user1WithoutPass;
    private User user2WithoutPass;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks

        // Datos de prueba con contraseña
        user1WithPass = new User(1L, "user1@mail.com", "User One", "hashed_pass_1", "01-01-2024 10:00:00", List.of(new Address(10L, "work", "street A", "UK")));
        user2WithPass = new User(2L, "user2@mail.com", "User Two", "hashed_pass_2", "01-01-2024 11:00:00", List.of(new Address(11L, "home", "street B", "AU")));

        // Datos de prueba sin contraseña
        user1WithoutPass = new User(1L, "user1@mail.com", "User One", null, "01-01-2024 10:00:00", List.of(new Address(10L, "work", "street A", "UK")));
        user2WithoutPass = new User(2L, "user2@mail.com", "User Two", null, "01-01-2024 11:00:00", List.of(new Address(11L, "home", "street B", "AU")));
    }

    @Test
    void testFindAllUsers_NoSort() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1WithPass, user2WithPass));

        List<User> users = userService.findAllUsers(null);

        assertFalse(users.isEmpty(), "La lista de usuarios no debería estar vacía");
        assertEquals(2, users.size(), "Debería haber 2 usuarios");
        assertNull(users.get(0).getPassword(), "La contraseña del primer usuario debería ser nula");
        assertNull(users.get(1).getPassword(), "La contraseña del segundo usuario debería ser nula");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindAllUsers_SortedById() {
        User userA = new User(3L, "a@mail.com", "Charlie", "pass_c", "01-01-2024 12:00:00", null);
        User userB = new User(1L, "b@mail.com", "Alice", "pass_a", "01-01-2024 10:00:00", null);
        User userC = new User(2L, "c@mail.com", "Bob", "pass_b", "01-01-2024 11:00:00", null);

        when(userRepository.findAll()).thenReturn(Arrays.asList(userA, userB, userC));

        List<User> users = userService.findAllUsers("id");
        assertEquals(3, users.size(), "Debería haber 3 usuarios");
        assertEquals(1L, users.get(0).getId(), "El primer usuario debería tener ID 1");
        assertEquals(2L, users.get(1).getId(), "El segundo usuario debería tener ID 2");
        assertEquals(3L, users.get(2).getId(), "El tercer usuario debería tener ID 3");
        assertNull(users.get(0).getPassword(), "La contraseña debería ser nula después del ordenamiento");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1WithPass));

        Optional<User> foundUser = userService.findUserById(1L);

        assertTrue(foundUser.isPresent(), "El usuario debería ser encontrado");
        assertEquals("user1@mail.com", foundUser.get().getEmail(), "El email del usuario no coincide");
        assertNull(foundUser.get().getPassword(), "La contraseña debería ser nula (removida por el servicio)");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findUserById(99L);

        assertFalse(foundUser.isPresent(), "El usuario no debería ser encontrado");
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void testCreateUser() {
        User newUserInput = new User(null, "new@mail.com", "New User", "raw_pass", null, null);
        when(userRepository.save(any(User.class))).thenReturn(user1WithPass);

        User createdUser = userService.createUser(newUserInput);

        assertNotNull(createdUser, "El usuario creado no debería ser nulo");
        assertNull(createdUser.getPassword(), "La contraseña debería ser nula (removida por el servicio)");
        assertEquals(user1WithoutPass.getEmail(), createdUser.getEmail(), "El email del usuario creado no coincide");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.deleteById(1L)).thenReturn(true);

        boolean deleted = userService.deleteUser(1L);

        assertTrue(deleted, "El usuario debería haber sido eliminado exitosamente");
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.deleteById(99L)).thenReturn(false);

        boolean deleted = userService.deleteUser(99L);

        assertFalse(deleted, "El usuario no debería haber sido eliminado (no encontrado)");
        verify(userRepository, times(1)).deleteById(99L);
    }

    @Test
    void testUpdateUser_Success() {
        User updates = new User();
        updates.setName("Updated Name");
        updates.setEmail("updated@mail.com");
        updates.setPassword("new_raw_pass");

        User userAfterRepoUpdate = new User(1L, "updated@mail.com", "Updated Name", "new_hashed_pass", "01-01-2024 10:00:00", null);
        when(userRepository.update(eq(1L), any(User.class))).thenReturn(Optional.of(userAfterRepoUpdate));

        Optional<User> updatedUser = userService.updateUser(1L, updates);

        assertTrue(updatedUser.isPresent(), "El usuario debería haber sido actualizado");
        assertEquals("Updated Name", updatedUser.get().getName(), "El nombre no se actualizó correctamente");
        assertNull(updatedUser.get().getPassword(), "La contraseña debería ser nula (removida por el servicio)");
        verify(userRepository, times(1)).update(eq(1L), any(User.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        User updates = new User();
        updates.setName("Updated Name");

        when(userRepository.update(eq(99L), any(User.class))).thenReturn(Optional.empty());

        Optional<User> updatedUser = userService.updateUser(99L, updates);

        assertFalse(updatedUser.isPresent(), "El usuario no debería ser encontrado para actualizar");
        verify(userRepository, times(1)).update(eq(99L), any(User.class));
    }

    @Test
    void testFindAddressesByUserId_Found() {
        List<Address> addresses = Arrays.asList(new Address(1L, "work", "street", "UK"));
        when(userRepository.findAddressesByUserId(1L)).thenReturn(Optional.of(addresses));

        Optional<List<Address>> foundAddresses = userService.findAddressesByUserId(1L);

        assertTrue(foundAddresses.isPresent(), "Debería encontrar las direcciones");
        assertFalse(foundAddresses.get().isEmpty(), "La lista de direcciones no debería estar vacía");
        verify(userRepository, times(1)).findAddressesByUserId(1L);
    }

    @Test
    void testUpdateAddress_Success() {
        Address updatedAddressDetails = new Address(null, "New Name", "New Street", "US");
        Address returnedAddress = new Address(10L, "New Name", "New Street", "US");

        when(userRepository.updateAddress(eq(1L), eq(10L), any(Address.class))).thenReturn(Optional.of(returnedAddress));

        Optional<Address> result = userService.updateAddress(1L, 10L, updatedAddressDetails);

        assertTrue(result.isPresent(), "La dirección debería haber sido actualizada");
        assertEquals("New Name", result.get().getName(), "El nombre de la dirección no coincide");
        verify(userRepository, times(1)).updateAddress(eq(1L), eq(10L), any(Address.class));
    }
}