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

import com.nathangtg.hotel_management.api.models.Room;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.RoomService;
import com.nathangtg.hotel_management.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Room> getAllRooms(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) String roomType) {
        
        // Only authenticated users can view rooms, but only admin/staff can modify
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (hotelId != null) {
            return roomService.getRoomsByHotelId(hotelId);
        } else if (isAvailable != null && isAvailable) {
            return roomService.getAvailableRooms();
        } else if (roomType != null) {
            return roomService.getRoomsByType(roomType);
        } else {
            return roomService.getAllRooms();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomService.getRoomById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        // All authenticated users can view room details
        return room.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Room createRoom(@Valid @RequestBody Room room) {
        // Only admin and staff can create rooms
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("STAFF")) {
            throw new RuntimeException("Access denied: Only admin and staff can create rooms");
        }
        
        return roomService.createRoom(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody Room room) {
        // Only admin and staff can update rooms
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("STAFF")) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Room updatedRoom = roomService.updateRoom(id, room);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        // Only admin and staff can delete rooms
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("STAFF")) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}