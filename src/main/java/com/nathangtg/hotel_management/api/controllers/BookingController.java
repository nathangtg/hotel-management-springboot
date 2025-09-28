package com.nathangtg.hotel_management.api.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.nathangtg.hotel_management.api.models.Booking;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.BookingService;
import com.nathangtg.hotel_management.services.UserService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Booking> getAllBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Only allow users to see their own bookings unless they are an admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (userId != null) {
            // Check if user is trying to access their own bookings or is an admin
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            if (!currentUsername.equals(String.valueOf(userId)) && !currentUser.getRole().equals("ADMIN")) {
                // If not accessing their own bookings and not an admin, restrict access
                return List.of(); // Return empty list or throw exception
            }
            return bookingService.getBookingsByUserId(userId);
        } else if (roomId != null) {
            return bookingService.getBookingsByRoomId(roomId);
        } else if (status != null) {
            return bookingService.getBookingsByStatus(status);
        } else if (startDate != null && endDate != null) {
            return bookingService.getBookingsByDateRange(startDate, endDate);
        } else {
            // Only admin can see all bookings
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            if (currentUser.getRole().equals("ADMIN")) {
                return bookingService.getAllBookings();
            } else {
                // Regular users can only see their own bookings
                return bookingService.getBookingsByUserId(currentUser.getId());
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        
        if (booking.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // Users can only access their own bookings unless they are an admin
            if (!booking.get().getUserId().equals(currentUser.getId()) && !currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(booking.get());
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        // Set the user ID to the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        
        // Set the user ID on the booking to the current user
        booking.setUser(currentUser);
        booking.setUserId(currentUser.getId());
        
        return bookingService.createBooking(booking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        Optional<Booking> existingBooking = bookingService.getBookingById(id);
        
        if (existingBooking.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // Only the booking owner or admin can update the booking
            if (!existingBooking.get().getUserId().equals(currentUser.getId()) && !currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            try {
                Booking updatedBooking = bookingService.updateBooking(id, booking);
                return ResponseEntity.ok(updatedBooking);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }
        
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        Optional<Booking> existingBooking = bookingService.getBookingById(id);
        
        if (existingBooking.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // Only the booking owner or admin can cancel the booking
            if (!existingBooking.get().getUserId().equals(currentUser.getId()) && !currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            try {
                bookingService.cancelBooking(id);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        Optional<Booking> existingBooking = bookingService.getBookingById(id);
        
        if (existingBooking.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            // Only the booking owner or admin can delete the booking
            if (!existingBooking.get().getUserId().equals(currentUser.getId()) && !currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            try {
                bookingService.deleteBooking(id);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }
        
        return ResponseEntity.notFound().build();
    }
}