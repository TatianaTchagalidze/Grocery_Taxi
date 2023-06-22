package com.example.grocery_taxi.service;

import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public boolean authenticateUser(String email, String password) {
    Optional<User> optionalUser = userRepository.findByEmail(email);
    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      return passwordEncoder.matches(password, user.getPassword());
    }
    return false;
  }
}
