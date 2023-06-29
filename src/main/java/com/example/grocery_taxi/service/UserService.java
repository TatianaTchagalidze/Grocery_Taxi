package com.example.grocery_taxi.service;

import com.example.grocery_taxi.dto.UserDto;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.model.UserRole;
import com.example.grocery_taxi.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserDto userDto) {
        validatePassword(userDto.getPassword(), userDto.getPasswordConfirmation());

        User user = User.builder()
            .email(userDto.getEmail())
            .firstName(userDto.getFirstName())
            .lastName(userDto.getLastName())
            .role(UserRole.valueOf(userDto.getRole()))
            .password(passwordEncoder.encode(userDto.getPassword()))
            .build();

        userRepository.save(user);
        return user;
    }


    private void validatePassword(String password, String passwordConfirmation) {
        if (!password.equals(passwordConfirmation)) {
            throw new IllegalArgumentException("Password confirmation does not match the password");
        }
    }


    public boolean emailExists(String email) {

        Optional<User> existingUser = userRepository.findByEmail(email);

        // Check if an existing user with the given email was found
        return existingUser.isPresent();
    }
}

