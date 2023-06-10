package com.example.grocery_taxi.service;

import com.example.grocery_taxi.model.User;
import com.example.grocery_taxi.model.UserRole;
import com.example.grocery_taxi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public void registerUser(User user, String passwordConfirmation) {
        validateUser(user, passwordConfirmation);

        // Encode the user's password before storing it
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Save the user in the repository
        userRepository.save(user);
    }

    private void validateUser(User user, String passwordConfirmation) {
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
        validatePasswordConfirmation(user.getPassword(), passwordConfirmation);
        validateUserRole(user.getRole());
        // Add any other validation logic specific to your application
    }

    private void validateEmail(String email) {
        // Use a regular expression pattern to validate the email format
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and numbers");
        }
    }

    private void validatePasswordConfirmation(String password, String passwordConfirmation) {
        if (!password.equals(passwordConfirmation)) {
            throw new IllegalArgumentException("Password confirmation does not match the password");
        }
    }

    private void validateUserRole(UserRole role) {
        if (role != UserRole.Customer && role != UserRole.Courier) {
            throw new IllegalArgumentException("Invalid user role");
        }
    }

}
