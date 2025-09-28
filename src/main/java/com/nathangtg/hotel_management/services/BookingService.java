package com.nathangtg.hotel_management.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nathangtg.hotel_management.api.models.Booking;
import com.nathangtg.hotel_management.api.models.Room;
import com.nathangtg.hotel_management.api.repositories.BookingRepository;
import com.nathangtg.hotel_management.api.repositories.RoomRepository;

@Service
public class BookingService {

    @Autowired
    public BookingRepository bookingRepository;
    
    @Autowired
    public RoomRepository roomRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_Id(userId);
    }

    public List<Booking> getBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoom_Id(roomId);
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }

    public List<Booking> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByCheckInDateBetween(startDate, endDate);
    }

    public Booking createBooking(Booking booking) {
        // Calculate total price based on room price and number of nights
        if (booking.getCheckInDate().isAfter(booking.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }
        
        Room room = roomRepository.findById(booking.getRoom().getId())
            .orElseThrow(() -> new RuntimeException("Room not found with id: " + booking.getRoom().getId()));
            
        if (!room.getIsAvailable()) {
            throw new RuntimeException("Room is not available for the selected dates");
        }
        
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        booking.setTotalPrice(totalPrice);
        
        // Set the userId from the user object if it's not already set
        if (booking.getUserId() == null && booking.getUser() != null) {
            booking.setUserId(booking.getUser().getId());
        }
        
        // Mark room as unavailable
        room.setIsAvailable(false);
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }

    public Booking updateBooking(Long id, Booking bookingDetails) {
        Booking existingBooking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
            
        // Only allow status updates after initial booking
        existingBooking.setStatus(bookingDetails.getStatus());
        
        return bookingRepository.save(existingBooking);
    }

    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
            
        if (booking.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Booking is already cancelled");
        }
        
        // Mark the associated room as available again
        Room room = booking.getRoom();
        room.setIsAvailable(true);
        roomRepository.save(room);
        
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }
    
    public void deleteBooking(Long id) {
        if (bookingRepository.existsById(id)) {
            // Before deleting, make sure to mark the room as available if not cancelled
            Booking booking = bookingRepository.findById(id).orElse(null);
            if (booking != null && !booking.getStatus().equals("CANCELLED")) {
                Room room = booking.getRoom();
                room.setIsAvailable(true);
                roomRepository.save(room);
            }
            bookingRepository.deleteById(id);
        } else {
            throw new RuntimeException("Booking not found with id: " + id);
        }
    }
}