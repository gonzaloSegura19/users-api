package com.chakray.usersapi.repository;

import com.chakray.usersapi.model.Address;
import com.chakray.usersapi.model.User;
import com.chakray.usersapi.util.PasswordHasher; // Asegúrate de que esta clase exista
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong userIdCounter = new AtomicLong(125);
    private final AtomicLong addressIdCounter = new AtomicLong(3);

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private final ZoneId ukZone = ZoneId.of("Europe/London");
    @PostConstruct
    public void init() {
        users.clear();
        userIdCounter.set(125);
        addressIdCounter.set(3);
        // Usuario 1
        List<Address> user1Addresses = new ArrayList<>();
        user1Addresses.add(new Address(1L, "workaddress", "street No. 1", "UK"));
        user1Addresses.add(new Address(2L, "homeaddress", "street No. 2", "AU"));
        users.add(new User(
                123L,
                "user1@mail.com",
                "user1",
                PasswordHasher.hashSha1("123456"), // Contraseña hasheada
                "01-01-2024 00:00:00",
                user1Addresses
        ));

        // Usuario 2
        List<Address> user2Addresses = new ArrayList<>();
        user2Addresses.add(new Address(3L, "vacationhome", "beach street", "ES"));
        users.add(new User(
                124L,
                "user2@mail.com",
                "user2",
                PasswordHasher.hashSha1("password123"), // Contraseña hasheada
                LocalDateTime.now(ukZone).format(formatter), // Fecha actual
                user2Addresses
        ));

        // Usuario 3
        List<Address> user3Addresses = new ArrayList<>();
        users.add(new User(
                125L,
                "user3@mail.com",
                "user3",
                PasswordHasher.hashSha1("securepass"), // Contraseña hasheada
                LocalDateTime.now(ukZone).format(formatter), // Fecha actual
                user3Addresses
        ));
    }

    @Override
    public List<User> findAll() {
        return Collections.unmodifiableList(users);
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(userIdCounter.incrementAndGet());
        }
        user.setPassword(PasswordHasher.hashSha1(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now(ukZone).format(formatter));

        // Asignar IDs a las direcciones si son nuevas
        if (user.getAddresses() != null) {
            user.getAddresses().forEach(address -> {
                if (address.getId() == null) {
                    address.setId(generateNewAddressId());
                }
            });
        }
        users.add(user);
        return user;
    }

    @Override
    public boolean deleteById(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }

    @Override
    public Optional<User> update(Long id, User updatedUser) {
        Optional<User> existingUserOpt = findById(id);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (updatedUser.getEmail() != null) {
                existingUser.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getName() != null) {
                existingUser.setName(updatedUser.getName());
            }
            if (updatedUser.getPassword() != null) {
                existingUser.setPassword(PasswordHasher.hashSha1(updatedUser.getPassword()));
            }

            return Optional.of(existingUser);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<Address>> findAddressesByUserId(Long userId) {
        return findById(userId)
                .map(User::getAddresses)
                .map(ArrayList::new);
    }

    @Override
    public Optional<Address> findAddressById(Long userId, Long addressId) {
        return findById(userId)
                .flatMap(user -> user.getAddresses().stream()
                        .filter(address -> address.getId().equals(addressId))
                        .findFirst());
    }

    @Override
    public Optional<Address> updateAddress(Long userId, Long addressId, Address updatedAddress) {
        return findById(userId).flatMap(user -> {
            Optional<Address> existingAddressOpt = user.getAddresses().stream()
                    .filter(addr -> addr.getId().equals(addressId))
                    .findFirst();

            if (existingAddressOpt.isPresent()) {
                Address existingAddress = existingAddressOpt.get();
                if (updatedAddress.getName() != null) {
                    existingAddress.setName(updatedAddress.getName());
                }
                if (updatedAddress.getStreet() != null) {
                    existingAddress.setStreet(updatedAddress.getStreet());
                }
                if (updatedAddress.getCountryCode() != null) {
                    existingAddress.setCountryCode(updatedAddress.getCountryCode());
                }
                return Optional.of(existingAddress);
            }
            return Optional.empty();
        });
    }

    @Override
    public Long generateNewAddressId() {
        return addressIdCounter.incrementAndGet();
    }
}