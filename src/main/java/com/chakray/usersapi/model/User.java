package com.chakray.usersapi.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;


import java.util.List;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    private String email;
    private String name;
    private String password;
    private String createdAt;
    private List<Address> addresses = new ArrayList<>();

    public User(Long id, String email, String name, String password, String createdAt, List<Address> addresses) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.createdAt = createdAt;
        this.addresses = addresses;
    }
}