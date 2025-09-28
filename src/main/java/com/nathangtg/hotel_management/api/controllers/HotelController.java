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

import com.nathangtg.hotel_management.api.models.Hotel;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.HotelService;
import com.nathangtg.hotel_management.services.UserService;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable Long id) {
        Optional<Hotel> hotel = hotelService.getHotelById(id);
        
        // All authenticated users can view hotel details
        return hotel.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Hotel createHotel(@RequestBody Hotel hotel) {
        // Only admin can create hotels
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN")) {
            throw new RuntimeException("Access denied: Only admin can create hotels");
        }
        
        return hotelService.createHotel(hotel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hotel> updateHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        // Only admin can update hotels
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Hotel updatedHotel = hotelService.updateHotel(id, hotel);
            return ResponseEntity.ok(updatedHotel);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        // Only admin can delete hotels
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        if (!currentUser.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            hotelService.deleteHotel(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}