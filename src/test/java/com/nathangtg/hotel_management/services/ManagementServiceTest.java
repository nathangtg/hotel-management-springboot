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

import com.nathangtg.hotel_management.api.models.Management;
import com.nathangtg.hotel_management.api.models.Hotel;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.api.repositories.ManagementRepository;

@ExtendWith(MockitoExtension.class)
class ManagementServiceTest {

    @Mock
    private ManagementRepository managementRepository;
    
    @InjectMocks
    private ManagementService managementService;
    
    private Management testManagement;
    private Hotel testHotel;
    private User testUser;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        testManagement = new Management();
        testManagement.setId(1L);
        testManagement.setHotel(testHotel);
        testManagement.setUser(testUser);
    }

    @Test
    void testGetAllManagements() {
        // Arrange
        List<Management> managements = Arrays.asList(testManagement);
        when(managementRepository.findAll()).thenReturn(managements);

        // Act
        List<Management> result = managementService.getAllManagements();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testManagement.getId(), result.get(0).getId());
        verify(managementRepository).findAll();
    }

    @Test
    void testGetManagementById() {
        // Arrange
        when(managementRepository.findById(1L)).thenReturn(Optional.of(testManagement));

        // Act
        Optional<Management> result = managementService.getManagementById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testManagement.getId(), result.get().getId());
        verify(managementRepository).findById(1L);
    }

    @Test
    void testGetManagementById_NotFound() {
        // Arrange
        when(managementRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Management> result = managementService.getManagementById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(managementRepository).findById(1L);
    }

    @Test
    void testCreateManagement() {
        // Arrange
        when(managementRepository.save(any(Management.class))).thenReturn(testManagement);

        // Act
        Management result = managementService.createManagement(testManagement);

        // Assert
        assertNotNull(result);
        assertEquals(testManagement.getId(), result.getId());
        verify(managementRepository).save(testManagement);
    }

    @Test
    void testUpdateManagement() {
        // Arrange
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        
        Management updatedManagement = new Management();
        updatedManagement.setId(1L);
        updatedManagement.setUser(newUser);
        updatedManagement.setHotel(testHotel);

        when(managementRepository.existsById(1L)).thenReturn(true);
        when(managementRepository.save(any(Management.class))).thenReturn(updatedManagement);

        // Act
        Management result = managementService.updateManagement(1L, updatedManagement);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getUser().getId());
        verify(managementRepository).existsById(1L);
        verify(managementRepository).save(updatedManagement);
    }

    @Test
    void testDeleteManagement() {
        // Arrange
        when(managementRepository.existsById(1L)).thenReturn(true);

        // Act
        managementService.deleteManagement(1L);

        // Assert
        verify(managementRepository).existsById(1L);
        verify(managementRepository).deleteById(1L);
    }
}
