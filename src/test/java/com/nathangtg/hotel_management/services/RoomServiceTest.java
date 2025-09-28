package com.nathangtg.hotel_management.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nathangtg.hotel_management.api.models.Room;
import com.nathangtg.hotel_management.api.repositories.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    
    @InjectMocks
    private RoomService roomService;
    
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Single");
        testRoom.setPricePerNight(new BigDecimal("100.00"));
        testRoom.setIsAvailable(true);
    }

    @Test
    void testGetAllRooms() {
        // Arrange
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        // Act
        List<Room> result = roomService.getAllRooms();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRoom.getRoomNumber(), result.get(0).getRoomNumber());
        verify(roomRepository).findAll();
    }

    @Test
    void testGetRoomById() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // Act
        Optional<Room> result = roomService.getRoomById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRoom.getRoomNumber(), result.get().getRoomNumber());
        verify(roomRepository).findById(1L);
    }

    @Test
    void testGetRoomById_NotFound() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Room> result = roomService.getRoomById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(roomRepository).findById(1L);
    }

    @Test
    void testGetRoomsByHotelId() {
        // Arrange
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findByHotelId(1L)).thenReturn(rooms);

        // Act
        List<Room> result = roomService.getRoomsByHotelId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRoom.getId(), result.get(0).getId());
        verify(roomRepository).findByHotelId(1L);
    }

    @Test
    void testGetAvailableRooms() {
        // Arrange
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findByIsAvailableTrue()).thenReturn(rooms);

        // Act
        List<Room> result = roomService.getAvailableRooms();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsAvailable());
        verify(roomRepository).findByIsAvailableTrue();
    }

    @Test
    void testCreateRoom() {
        // Arrange
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // Act
        Room result = roomService.createRoom(testRoom);

        // Assert
        assertNotNull(result);
        assertEquals(testRoom.getRoomNumber(), result.getRoomNumber());
        verify(roomRepository).save(testRoom);
    }

    @Test
    void testUpdateRoom() {
        // Arrange
        Room updatedRoom = new Room();
        updatedRoom.setId(1L);
        updatedRoom.setRoomNumber("102");

        when(roomRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.save(any(Room.class))).thenReturn(updatedRoom);

        // Act
        Room result = roomService.updateRoom(1L, updatedRoom);

        // Assert
        assertNotNull(result);
        assertEquals("102", result.getRoomNumber());
        verify(roomRepository).existsById(1L);
        verify(roomRepository).save(updatedRoom);
    }

    @Test
    void testDeleteRoom() {
        // Arrange
        when(roomRepository.existsById(1L)).thenReturn(true);

        // Act
        roomService.deleteRoom(1L);

        // Assert
        verify(roomRepository).existsById(1L);
        verify(roomRepository).deleteById(1L);
    }
}
