package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.dto.UserDto;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.repository.UserRepository;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



@RestController
@AllArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/users")
  public ResponseEntity<User> registerUser(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      // Handle validation errors and return appropriate response
      return ResponseEntity.badRequest().build();
    } else {
      User user = User.builder()
          .email(userDto.getEmail())
          .firstName(userDto.getFirstName())
          .lastName(userDto.getLastName())
          .role(userDto.getRole())
          .password(passwordEncoder.encode(userDto.getPassword()))
          .build();

      // Save the user in the database
      User createdUser = userRepository.save(user);

      // Return the created user entity with 201 HTTP status code
      return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
  }
}
