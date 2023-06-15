package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.dto.UserDto;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.ApiError;
import com.example.grocery_taxi.service.UserService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;


@RestController
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/users")
  public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto,
                                        BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      // Handle validation errors and return appropriate response
      List<String> validationErrors = bindingResult.getFieldErrors()
          .stream()
          .map(FieldError::getDefaultMessage)
          .collect(Collectors.toList());

      ApiError apiError =
          new ApiError(HttpStatus.BAD_REQUEST, LocalDateTime.now(), "Validation Error",
              validationErrors);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    User createdUser = userService.registerUser(userDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }
}
