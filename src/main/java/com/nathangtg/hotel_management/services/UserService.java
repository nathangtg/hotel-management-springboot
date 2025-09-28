package com.nathangtg.hotel_management.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.api.repositories.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    protected PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public User findByIdAndRole(Long id, String role) {
        return userRepository.findByIdAndRole(id, role)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id + " and role: " + role));
    }

    public User findByUsernameAndRole(String username, String role) {
        return userRepository.findByUsernameAndRole(username, role)
        .orElseThrow(() -> new RuntimeException("User not found with username: " + username + " and role: " + role));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public List<User> findByRoleIn(List<String> roles) {
        return userRepository.findByRoleIn(roles);
    }

    public User findByFirstNameAndLastName(String firstName, String lastName) {
        return userRepository.findByFirstNameAndLastName(firstName, lastName)
        .orElseThrow(() -> new RuntimeException("User not found with name: " + firstName + " " + lastName));
    }

    public User createUser(User user) {
        // Check if username or email already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            
        // Check if username or email changes and if new values already exist
        if (!existingUser.getUsername().equals(userDetails.getUsername()) 
            && userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + userDetails.getUsername());
        }
        if (!existingUser.getEmail().equals(userDetails.getEmail()) 
            && userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + userDetails.getEmail());
        }
        
        // Update user details
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setAddress(userDetails.getAddress());
        existingUser.setRole(userDetails.getRole());
        
        // Only update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }

    public User saveUser(User user) {
        // If saving an existing user, encode password only if it's changed
        if (user.getId() == null || user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    public List<User> findAllByRole(String role) {
        return userRepository.findAllByRole(role)
        .orElseThrow(() -> new RuntimeException("No users found with role: " + role));
    }
}
