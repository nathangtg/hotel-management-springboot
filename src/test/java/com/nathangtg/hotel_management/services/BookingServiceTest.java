package com.nathangtg.hotel_management.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nathangtg.hotel_management.api.models.Booking;
import com.nathangtg.hotel_management.api.models.Room;
import com.nathangtg.hotel_management.api.repositories.BookingRepository;
import com.nathangtg.hotel_management.api.repositories.RoomRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private RoomRepository roomRepository;
    
    @InjectMocks
    private BookingService bookingService;
    
    private Booking testBooking;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber("101");
        testRoom.setPricePerNight(new BigDecimal("100.00"));
        testRoom.setIsAvailable(true);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUserId(1L);
        testBooking.setRoomId(1L);
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setTotalPrice(new BigDecimal("200.00"));
        testBooking.setStatus("CONFIRMED");
    }

    @Test
    void testGetAllBookings() {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        // Act
        List<Booking> result = bookingService.getAllBookings();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBooking.getId(), result.get(0).getId());
        verify(bookingRepository).findAll();
    }

    @Test
    void testGetBookingById() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // Act
        Optional<Booking> result = bookingService.getBookingById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testBooking.getId(), result.get().getId());
        verify(bookingRepository).findById(1L);
    }

    @Test
    void testGetBookingById_NotFound() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Booking> result = bookingService.getBookingById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(bookingRepository).findById(1L);
    }

    @Test
    void testGetBookingsByUserId() {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findByUser_Id(1L)).thenReturn(bookings);

        // Act
        List<Booking> result = bookingService.getBookingsByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBooking.getUserId(), result.get(0).getUserId());
        verify(bookingRepository).findByUser_Id(1L);
    }

    @Test
    void testGetBookingsByStatus() {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findByStatus("CONFIRMED")).thenReturn(bookings);

        // Act
        List<Booking> result = bookingService.getBookingsByStatus("CONFIRMED");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CONFIRMED", result.get(0).getStatus());
        verify(bookingRepository).findByStatus("CONFIRMED");
    }

    @Test
    void testCreateBooking() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Act
        Booking result = bookingService.createBooking(testBooking);

        // Assert
        assertNotNull(result);
        assertEquals(testBooking.getId(), result.getId());
        verify(roomRepository).findById(1L);
        verify(bookingRepository).save(testBooking);
    }

    @Test
    void testUpdateBooking() {
        // Arrange
        Booking updatedBooking = new Booking();
        updatedBooking.setId(1L);
        updatedBooking.setStatus("CANCELLED");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);

        // Act
        Booking result = bookingService.updateBooking(1L, updatedBooking);

        // Assert
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testCancelBooking() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // Act
        bookingService.cancelBooking(1L);

        // Assert
        verify(bookingRepository).findById(1L);
    }

    @Test
    void testDeleteBooking() {
        // Arrange
        when(bookingRepository.existsById(1L)).thenReturn(true);

        // Act
        bookingService.deleteBooking(1L);

        // Assert
        verify(bookingRepository).existsById(1L);
        verify(bookingRepository).deleteById(1L);
    }
}
