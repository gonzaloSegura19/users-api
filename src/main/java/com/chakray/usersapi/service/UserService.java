package com.chakray.usersapi.service;

import com.chakray.usersapi.model.Address;
import com.chakray.usersapi.model.User;
import com.chakray.usersapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Método auxiliar para remover la contraseña antes de retornar el usuario al cliente
    private User removePassword(User user) {
        if (user == null) {
            return null;
        }
        User userWithoutPassword = new User();
        userWithoutPassword.setId(user.getId());
        userWithoutPassword.setEmail(user.getEmail());
        userWithoutPassword.setName(user.getName());
        userWithoutPassword.setCreatedAt(user.getCreatedAt());
        userWithoutPassword.setAddresses(user.getAddresses() != null ? new ArrayList<>(user.getAddresses()) : new ArrayList<>());
        return userWithoutPassword;
    }

    public List<User> findAllUsers(String sortedBy) {
        List<User> users = userRepository.findAll();

        if (sortedBy != null && !sortedBy.isEmpty()) {
            Comparator<User> comparator = null;
            switch (sortedBy.toLowerCase()) {
                case "email":
                    comparator = Comparator.comparing(User::getEmail);
                    break;
                case "id":
                    comparator = Comparator.comparing(User::getId);
                    break;
                case "name":
                    comparator = Comparator.comparing(User::getName);
                    break;
                case "created_at":
                    comparator = Comparator.comparing(User::getCreatedAt);
                    break;
                default:
                    // Si el parámetro no es válido, no se ordena
                    break;
            }
            if (comparator != null) {
                users = users.stream().sorted(comparator).collect(Collectors.toList());
            }
        }
        // Remover contraseñas antes de retornar a la capa de presentación (controlador)
        return users.stream()
                .map(this::removePassword)
                .collect(Collectors.toList());
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id)
                .map(this::removePassword); // Remover contraseña si se encuentra
    }

    public User createUser(User user) {
        // El repositorio ya hashea la contraseña y establece created_at
        User savedUser = userRepository.save(user);
        return removePassword(savedUser); // Retornar sin contraseña
    }

    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id);
    }

    public Optional<User> updateUser(Long id, User userDetails) {
        return userRepository.update(id, userDetails)
                .map(this::removePassword); // Remover contraseña si se actualiza
    }

    public Optional<List<Address>> findAddressesByUserId(Long userId) {
        return userRepository.findAddressesByUserId(userId);
    }

    public Optional<Address> updateAddress(Long userId, Long addressId, Address updatedAddress) {
        return userRepository.updateAddress(userId, addressId, updatedAddress);
    }
}