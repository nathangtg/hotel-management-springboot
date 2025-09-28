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
import org.springframework.web.bind.annotation.RestController;

import com.nathangtg.hotel_management.api.models.Management;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.ManagementService;
import com.nathangtg.hotel_management.services.UserService;

@RestController
@RequestMapping("/api/managements")
public class ManagementController {

    @Autowired
    private ManagementService managementService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Management> getAllManagements() {
        // Only admin can see all managements
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN")) {
            // Non-admin users shouldn't have access
            return List.of(); // Return empty list
        }
        
        return managementService.getAllManagements();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Management> getManagementById(@PathVariable Long id) {
        Optional<Management> management = managementService.getManagementById(id);
        
        if (management.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // Only admin can access management details
            if (!currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            return ResponseEntity.ok(management.get());
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public Management createManagement(@RequestBody Management management) {
        // Only admin can create managements
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN")) {
            throw new RuntimeException("Access denied: Only admin can create management entries");
        }
        
        return managementService.createManagement(management);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Management> updateManagement(@PathVariable Long id, @RequestBody Management management) {
        Optional<Management> existingManagement = managementService.getManagementById(id);
        
        if (existingManagement.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // Only admin can update management details
            if (!currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            try {
                Management updatedManagement = managementService.updateManagement(id, management);
                return ResponseEntity.ok(updatedManagement);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManagement(@PathVariable Long id) {
        Optional<Management> existingManagement = managementService.getManagementById(id);
        
        if (existingManagement.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // Only admin can delete management entries
            if (!currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            try {
                managementService.deleteManagement(id);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }
        
        return ResponseEntity.notFound().build();
    }
}