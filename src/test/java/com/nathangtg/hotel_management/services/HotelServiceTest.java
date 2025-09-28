package com.nathangtg.hotel_management.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nathangtg.hotel_management.api.models.Hotel;
import com.nathangtg.hotel_management.api.repositories.HotelRepository;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;
    
    @InjectMocks
    private HotelService hotelService;
    
    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setAddress("123 Test Street");
        testHotel.setPhone("555-1234");
        testHotel.setEmail("test@hotel.com");
    }

    @Test
    void testGetAllHotels() {
        // Arrange
        List<Hotel> hotels = Arrays.asList(testHotel);
        when(hotelRepository.findAll()).thenReturn(hotels);

        // Act
        List<Hotel> result = hotelService.getAllHotels();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testHotel.getName(), result.get(0).getName());
        verify(hotelRepository).findAll();
    }

    @Test
    void testGetHotelById() {
        // Arrange
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));

        // Act
        Optional<Hotel> result = hotelService.getHotelById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testHotel.getName(), result.get().getName());
        verify(hotelRepository).findById(1L);
    }

    @Test
    void testGetHotelById_NotFound() {
        // Arrange
        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Hotel> result = hotelService.getHotelById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(hotelRepository).findById(1L);
    }

    @Test
    void testCreateHotel() {
        // Arrange
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);

        // Act
        Hotel result = hotelService.createHotel(testHotel);

        // Assert
        assertNotNull(result);
        assertEquals(testHotel.getName(), result.getName());
        verify(hotelRepository).save(testHotel);
    }

    @Test
    void testUpdateHotel() {
        // Arrange
        Hotel updatedHotel = new Hotel();
        updatedHotel.setId(1L);
        updatedHotel.setName("Updated Hotel");

        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(updatedHotel);

        // Act
        Hotel result = hotelService.updateHotel(1L, updatedHotel);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Hotel", result.getName());
        verify(hotelRepository).existsById(1L);
        verify(hotelRepository).save(updatedHotel);
    }

    @Test
    void testDeleteHotel() {
        // Arrange
        when(hotelRepository.existsById(1L)).thenReturn(true);

        // Act
        hotelService.deleteHotel(1L);

        // Assert
        verify(hotelRepository).existsById(1L);
        verify(hotelRepository).deleteById(1L);
    }
}
