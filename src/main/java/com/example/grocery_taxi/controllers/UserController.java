package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.dto.UserDto;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.service.UserService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import javax.validation.Valid;

@RestController
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/users")
  public ResponseEntity<User> registerUser(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      // Handle validation errors and return appropriate response
      return ResponseEntity.badRequest().build();
    }

    User createdUser = userService.registerUser(userDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }
}
