package com.chakray.usersapi.repository;

import com.chakray.usersapi.model.Address;
import com.chakray.usersapi.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(Long id);
    User save(User user);
    boolean deleteById(Long id);
    Optional<User> update(Long id, User updatedUser);


    Optional<List<Address>> findAddressesByUserId(Long userId);
    Optional<Address> findAddressById(Long userId, Long addressId);
    Optional<Address> updateAddress(Long userId, Long addressId, Address updatedAddress);
    Long generateNewAddressId();
