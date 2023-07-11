package com.example.grocery_taxi.entity;

import com.example.grocery_taxi.model.UserRole;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import net.minidev.json.annotate.JsonIgnore;

@Entity
@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @NotBlank
    @JsonIgnore
    @Size(min = 8)
    private String password;


    public User() {
    }
}
