package com.nathangtg.hotel_management.api.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String email) {
        
        // Only admin can see all users
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN")) {
            // Non-admin users can only see their own profile or search by specific parameters
            if (email != null && email.equals(currentUser.getEmail())) {
                User user = userService.findByEmail(email);
                return List.of(user); // Return single user as list
            } else {
                return List.of(currentUser); // Return only the current user
            }
        }
        
        if (role != null) {
            return userService.findAllByRole(role);
        } else if (email != null) {
            User user = userService.findByEmail(email);
            return List.of(user); // Return single user as list
        } else {
            return userService.findAll();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // Users can only access their own profile or if they are an admin
            if (!user.get().getId().equals(currentUser.getId()) && !currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            return ResponseEntity.ok(user.get());
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        // Only users can update their own profile or if they are an admin
        if (!currentUser.getId().equals(id) && !currentUser.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        // Only users can delete their own profile or if they are an admin
        if (!currentUser.getId().equals(id) && !currentUser.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    public List<User> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String role) {
        
        // Only admin can search users
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN")) {
            // Non-admin users can't perform general searches
            return List.of();
        }
        
        if (firstName != null && lastName != null) {
            User user = userService.findByFirstNameAndLastName(firstName, lastName);
            return List.of(user); // Return single user as list
        } else if (role != null) {
            return userService.findAllByRole(role);
        } else {
            return userService.findAll();
        }
    }
}