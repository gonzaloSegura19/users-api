package com.chakray.usersapi.controller;

import com.chakray.usersapi.model.Address;
import com.chakray.usersapi.model.User;
import com.chakray.usersapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users?sortedBy=[email|id|name|created_at]
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) String sortedBy) {
        List<User> users = userService.findAllUsers(sortedBy);
        return ResponseEntity.ok(users);
    }

    // GET /users/{user_id}/addresses
    @GetMapping("/{userId}/addresses")
    public ResponseEntity<List<Address>> getUserAddresses(@PathVariable Long userId) {
        Optional<List<Address>> addresses = userService.findAddressesByUserId(userId);
        return addresses.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /users/{user_id}/addresses/{address_id}
    @PutMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long userId,
                                                 @PathVariable Long addressId,
                                                 @RequestBody Address updatedAddress) {
        Optional<Address> address = userService.updateAddress(userId, addressId, updatedAddress);
        return address.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /users
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User newUser = userService.createUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // PATCH /users/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> updatedUser = userService.updateUser(id, userDetails);
        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}