package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.config.JwtUtil;
import com.example.grocery_taxi.dto.LoginRequestDto;
import com.example.grocery_taxi.dto.UserDto;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.ApiError;
import com.example.grocery_taxi.service.AuthenticationService;
import com.example.grocery_taxi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final AuthenticationService authenticationService;
  private final JwtUtil jwtUtil;

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response, @RequestBody @Valid LoginRequestDto loginRequestDtoForm) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    boolean isAuthenticated = authenticationService.authenticateUser(loginRequestDtoForm.getEmail(), loginRequestDtoForm.getPassword());
    if (isAuthenticated) {
      String token = jwtUtil.generateToken(loginRequestDtoForm.getEmail());

      // Create a map to represent the JSON structure
      Map<String, String> responseMap = new HashMap<>();
      responseMap.put("token", token);

      return ResponseEntity.ok(responseMap);
    } else {
      ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, LocalDateTime.now(), "Authentication Error",
          List.of("Invalid email or password"));
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }
  }

  @GetMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      new SecurityContextLogoutHandler().logout(request, response, authentication);
    }
    return ResponseEntity.ok("Logout successful");
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


   // The purpose of this code is to extract a token from the request, validate it using a JWT (JSON Web Token) utility, and return a response accordingly.
  @GetMapping("/secure-resource")
  public ResponseEntity<?> secureResource(HttpServletRequest request) {
    String token = extractTokenFromRequest(request);

    if (jwtUtil.validateToken(token)) {
      Authentication authentication = new UsernamePasswordAuthenticationToken(token, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // Create a map to represent the JSON structure
      Map<String, String> responseMap = new HashMap<>();
      responseMap.put("token", token);

      return ResponseEntity.ok(responseMap);
    } else {
      ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, LocalDateTime.now(), "Authentication Error",
          List.of("Invalid token"));
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
