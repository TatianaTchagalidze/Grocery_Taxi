package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.config.JwtUtil;
import com.example.grocery_taxi.dto.LoginRequestDto;
import com.example.grocery_taxi.dto.RegistrationResponseDto;
import com.example.grocery_taxi.dto.UserDto;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.ApiError;
import com.example.grocery_taxi.filter.JwtAuthenticationFilter;
import com.example.grocery_taxi.service.AuthenticationService;
import com.example.grocery_taxi.service.UserService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class UserController {

  private final UserService userService;
  private final AuthenticationService authenticationService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  private final JwtUtil jwtUtil;

  public UserController(UserService userService, AuthenticationService authenticationService,
                        JwtAuthenticationFilter jwtAuthenticationFilter, JwtUtil jwtUtil) {
    this.userService = userService;
    this.authenticationService = authenticationService;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response,
                                 @RequestBody @Valid LoginRequestDto loginRequestDtoForm) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    boolean isAuthenticated = authenticationService.authenticateUser(loginRequestDtoForm.getEmail(),
        loginRequestDtoForm.getPassword());
    if (isAuthenticated) {
      // Fetch the user from the database based on the email
      User user = userService.getUserByEmail(loginRequestDtoForm.getEmail());

      if (user != null) {
        // Add the user role to the response
        String role = user.getRole().name();
        long userId = user.getId(); // Assuming the method to get userId exists in the User class' // Assuming the role is an Enum type
        String token = jwtUtil.generateToken(loginRequestDtoForm.getEmail()); // Use the email as the username
        jwtAuthenticationFilter.saveTokenInCookies(request, response, token);

        // Create a custom response object containing the user role
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("role", role);
        responseBody.put("userId", userId);


        return ResponseEntity.ok(responseBody);
      }
    }

    // Authentication failed
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @RequestMapping(value = "/logout", method = RequestMethod.POST)
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    // Remove the JWT token from cookies using JwtAuthenticationFilter
    jwtAuthenticationFilter.removeTokenFromCookies(response);

    return ResponseEntity.ok("Logout successful");
  }

  @PostMapping("/users")
  public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
    // Check if email already exists
    if (userService.emailExists(userDto.getEmail())) {
      List<String> validationErrors = new ArrayList<>();
      validationErrors.add("Email already exists. Please use a different email.");

      ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, LocalDateTime.now(), "Validation Error", validationErrors);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    // Handle user registration
    if (bindingResult.hasErrors()) {
      List<String> validationErrors = bindingResult.getFieldErrors()
          .stream()
          .map(FieldError::getDefaultMessage)
          .collect(Collectors.toList());

      ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, LocalDateTime.now(), "Validation Error", validationErrors);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    RegistrationResponseDto createdUser = userService.registerUser(userDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }
  
  @GetMapping("/secure-resource")
  public ResponseEntity<?> secureResource() {
    // Access secure resource
    return ResponseEntity.ok("Secure resource accessed successfully");
  }
}