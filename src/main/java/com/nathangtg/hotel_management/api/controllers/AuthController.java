package com.nathangtg.hotel_management.api.controllers;

import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.security.JwtUtil;
import com.nathangtg.hotel_management.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Load user details and generate token with user ID and role
            User user = userService.findByUsername(loginRequest.getUsername());
            final String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");
            response.put("userId", String.valueOf(user.getId()));
            response.put("username", user.getUsername());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Check if user already exists
            if (userService.findByUsername(registerRequest.getUsername()) != null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Username already exists");
                return ResponseEntity.badRequest().body(response);
            }

            if (userService.findByEmail(registerRequest.getEmail()) != null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Email already exists");
                return ResponseEntity.badRequest().body(response);
            }

            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : "USER"); // Default role
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            // Save user
            User savedUser = userService.createUser(user);

            // Generate JWT token with user ID and role
            String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getId(), savedUser.getRole());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Registration successful");
            response.put("userId", String.valueOf(savedUser.getId()));
            response.put("username", savedUser.getUsername());
            response.put("role", savedUser.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // DTO classes for request bodies
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String password;
        private String role;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}