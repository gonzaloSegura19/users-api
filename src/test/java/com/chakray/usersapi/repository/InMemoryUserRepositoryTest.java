package com.chakray.usersapi.repository;

import com.chakray.usersapi.model.Address;
import com.chakray.usersapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InMemoryUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testFindAllUsers() {
        List<User> users = userRepository.findAll();
        assertFalse(users.isEmpty(), "La lista de usuarios no debería estar vacía");
        assertEquals(3, users.size(), "Debería haber 3 usuarios iniciales");
        assertNotNull(users.get(0).getPassword(), "La contraseña debería estar presente en el repositorio");
    }

    @Test
    void testFindUserById_ExistingUser() {
        Optional<User> user = userRepository.findById(123L);
        assertTrue(user.isPresent(), "El usuario con ID 123 debería existir");
        assertEquals("user1@mail.com", user.get().getEmail(), "El email del usuario no coincide");
        assertNotNull(user.get().getPassword(), "La contraseña debe estar presente a nivel de repositorio");
    }

    @Test
    void testFindUserById_NonExistingUser() {
        Optional<User> user = userRepository.findById(999L);
        assertFalse(user.isPresent(), "El usuario con ID 999 no debería existir");
    }

    @Test
    void testSaveUser() {
        User newUser = new User();
        newUser.setEmail("testuser@mail.com");
        newUser.setName("Test User");
        newUser.setPassword("testpass");
        newUser.setAddresses(List.of(new Address(null, "testaddress", "test street", "CA")));

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId(), "El ID del usuario guardado no debería ser nulo");
        assertEquals(126L, savedUser.getId(), "El ID del nuevo usuario debería ser 126");
        assertEquals("testuser@mail.com", savedUser.getEmail(), "El email del usuario guardado no coincide");
        assertNotNull(savedUser.getPassword(), "La contraseña hasheada debería estar presente");
        assertNotEquals("testpass", savedUser.getPassword(), "La contraseña no debería ser la original (debe estar hasheada)");
        assertNotNull(savedUser.getCreatedAt(), "La fecha de creación no debería ser nula");
        assertEquals(1, savedUser.getAddresses().size(), "Debería tener una dirección");
        assertNotNull(savedUser.getAddresses().get(0).getId(), "El ID de la dirección generada no debería ser nulo");
        assertEquals(4L, savedUser.getAddresses().get(0).getId(), "El ID de la dirección debería ser 4");


        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent(), "El usuario guardado debería poder ser encontrado por su ID");
        assertEquals("testuser@mail.com", foundUser.get().getEmail(), "El email del usuario encontrado no coincide");

        assertEquals(4, userRepository.findAll().size(), "Debería haber 4 usuarios después de guardar uno");
    }


    @Test
    void testDeleteUser_ExistingUser() {

        boolean deleted = userRepository.deleteById(123L);
        assertTrue(deleted, "El usuario con ID 123 debería haber sido eliminado exitosamente");
        Optional<User> user = userRepository.findById(123L);
        assertFalse(user.isPresent(), "El usuario con ID 123 no debería existir después de la eliminación");
        assertEquals(2, userRepository.findAll().size(), "Debería haber 2 usuarios después de la eliminación");
    }

    @Test
    void testDeleteUser_NonExistingUser() {
        boolean deleted = userRepository.deleteById(999L);
        assertFalse(deleted, "No se debería haber eliminado ningún usuario con ID 999");
    }

    @Test
    void testUpdateUserPartial() {
        User updates = new User();
        updates.setEmail("updated@mail.com");
        updates.setName("Updated User");
        updates.setPassword("newpass");

        Optional<User> updatedUser = userRepository.update(123L, updates);

        assertTrue(updatedUser.isPresent(), "El usuario con ID 123 debería haber sido actualizado");
        assertEquals("updated@mail.com", updatedUser.get().getEmail(), "El email no se actualizó correctamente");
        assertEquals("Updated User", updatedUser.get().getName(), "El nombre no se actualizó correctamente");
        assertNotNull(updatedUser.get().getPassword(), "La contraseña hasheada debería estar presente después de la actualización");
        assertNotEquals("newpass", updatedUser.get().getPassword(), "La nueva contraseña debería estar hasheada");
        assertEquals("01-01-2024 00:00:00", updatedUser.get().getCreatedAt(), "La fecha de creación no debería cambiar en un update");

        Optional<User> fetchedUser = userRepository.findById(123L);
        assertTrue(fetchedUser.isPresent());
        assertEquals("updated@mail.com", fetchedUser.get().getEmail());
    }

    @Test
    void testUpdateUserPartial_NonExistingUser() {
        User updates = new User();
        updates.setEmail("nonexistent@mail.com");

        Optional<User> updatedUser = userRepository.update(999L, updates);
        assertFalse(updatedUser.isPresent(), "No se debería haber actualizado un usuario no existente");
    }

    @Test
    void testFindAddressesByUserId_ExistingUser() {
        Optional<List<Address>> addresses = userRepository.findAddressesByUserId(123L);
        assertTrue(addresses.isPresent(), "Debería encontrar direcciones para el usuario 123");
        assertEquals(2, addresses.get().size(), "El usuario 123 debería tener 2 direcciones");
        assertEquals("workaddress", addresses.get().get(0).getName(), "El nombre de la primera dirección no coincide");
    }

    @Test
    void testFindAddressesByUserId_NonExistingUser() {
        Optional<List<Address>> addresses = userRepository.findAddressesByUserId(999L);
        assertFalse(addresses.isPresent(), "No debería encontrar direcciones para un usuario no existente");
    }

    @Test
    void testUpdateAddress_ExistingAddress() {
        Address updatedAddressDetails = new Address(null, "new name", "new street", "MX");


        Optional<Address> updatedAddress = userRepository.updateAddress(123L, 1L, updatedAddressDetails);

        assertTrue(updatedAddress.isPresent(), "La dirección debería haber sido actualizada");
        assertEquals("new name", updatedAddress.get().getName(), "El nombre de la dirección no se actualizó");
        assertEquals("new street", updatedAddress.get().getStreet(), "La calle de la dirección no se actualizó");
        assertEquals("MX", updatedAddress.get().getCountryCode(), "El código de país no se actualizó");


        Optional<User> userOpt = userRepository.findById(123L);
        assertTrue(userOpt.isPresent());
        Optional<Address> addressInUser = userOpt.get().getAddresses().stream()
                .filter(a -> a.getId().equals(1L))
                .findFirst();
        assertTrue(addressInUser.isPresent());
        assertEquals("new name", addressInUser.get().getName());
        assertEquals("new street", addressInUser.get().getStreet());
        assertEquals("MX", addressInUser.get().getCountryCode());
    }


    @Test
    void testUpdateAddress_NonExistingUser() {
        Address updatedAddressDetails = new Address(null, "name", "street", "MX");

        Optional<Address> updatedAddress = userRepository.updateAddress(999L, 1L, updatedAddressDetails);
        assertFalse(updatedAddress.isPresent(), "No debería actualizar dirección para un usuario no existente");
    }

    @Test
    void testUpdateAddress_NonExistingAddress() {
        Address updatedAddressDetails = new Address(null, "name", "street", "MX");

        Optional<Address> updatedAddress = userRepository.updateAddress(123L, 99L, updatedAddressDetails);
        assertFalse(updatedAddress.isPresent(), "No debería actualizar dirección no existente para un usuario");
    }

    @Test
    void testGenerateNewAddressId() {
        Long id1 = userRepository.generateNewAddressId();
        Long id2 = userRepository.generateNewAddressId();

        assertNotNull(id1, "El ID generado no debería ser nulo");
        assertNotNull(id2, "El segundo ID generado no debería ser nulo");
        assertTrue(id2 > id1, "Los IDs deberían ser incrementales");
        assertEquals(4L, id1, "El primer ID generado debería ser 4");
        assertEquals(5L, id2, "El segundo ID generado debería ser 5");
    }
}