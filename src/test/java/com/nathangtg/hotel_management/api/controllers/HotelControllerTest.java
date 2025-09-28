package com.nathangtg.hotel_management.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathangtg.hotel_management.api.models.Hotel;
import com.nathangtg.hotel_management.api.models.User;
import com.nathangtg.hotel_management.services.HotelService;
import com.nathangtg.hotel_management.services.UserService;
import com.nathangtg.hotel_management.test.config.ControllerTestConfiguration;

@SpringBootTest(classes = ControllerTestConfiguration.class, 
               properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("controller-test")
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private HotelService hotelService;
    
    @MockitoBean
    private UserService userService;
    
    private User testUser;
    private User adminUser;
    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");
        
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setAddress("123 Test Street");
        testHotel.setPhone("555-1234");
        testHotel.setEmail("test@hotel.com");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAllHotels_AsUser() throws Exception {
        // Arrange
        List<Hotel> hotels = Arrays.asList(testHotel);
        when(hotelService.getAllHotels()).thenReturn(hotels);
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/hotels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Test Hotel"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllHotels_AsAdmin() throws Exception {
        // Arrange
        List<Hotel> hotels = Arrays.asList(testHotel);
        when(hotelService.getAllHotels()).thenReturn(hotels);
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/hotels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetHotelById_AsUser() throws Exception {
        // Arrange
        when(hotelService.getHotelById(1L)).thenReturn(Optional.of(testHotel));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/hotels/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetHotelById_AsAdmin() throws Exception {
        // Arrange
        when(hotelService.getHotelById(1L)).thenReturn(Optional.of(testHotel));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/hotels/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetHotelById_NotFound() throws Exception {
        // Arrange
        when(hotelService.getHotelById(999L)).thenReturn(Optional.empty());
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/hotels/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateHotel_AsAdmin() throws Exception {
        // Arrange
        Hotel newHotel = new Hotel();
        newHotel.setName("New Hotel");
        newHotel.setAddress("456 New Street");
        newHotel.setPhone("555-5678");
        newHotel.setEmail("new@hotel.com");
        
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(hotelService.createHotel(any(Hotel.class))).thenReturn(newHotel);

        // Act & Assert
        mockMvc.perform(post("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newHotel)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("New Hotel"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateHotel_AsUser_ShouldReturnForbidden() throws Exception {
        // Arrange
        Hotel newHotel = new Hotel();
        newHotel.setName("New Hotel");
        newHotel.setAddress("456 New Street");
        newHotel.setPhone("555-5678");
        newHotel.setEmail("new@hotel.com");
        
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newHotel)))
            .andExpect(status().isForbidden()) // Access denied should return 403
            .andExpect(jsonPath("$.error").value("Access denied: Only admin can create hotels"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateHotel_AsAdmin() throws Exception {
        // Arrange
        Hotel updatedHotel = new Hotel();
        updatedHotel.setName("Updated Hotel");
        updatedHotel.setAddress("789 Updated Avenue");
        updatedHotel.setPhone("555-9012");
        updatedHotel.setEmail("updated@hotel.com");
        
        when(hotelService.getHotelById(1L)).thenReturn(Optional.of(testHotel));
        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(hotelService.updateHotel(eq(1L), any(Hotel.class))).thenReturn(updatedHotel);

        // Act & Assert
        mockMvc.perform(put("/api/hotels/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedHotel)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Hotel"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateHotel_AsUser_ShouldReturnForbidden() throws Exception {
        // Arrange
        Hotel updatedHotel = new Hotel();
        updatedHotel.setName("Updated Hotel");
        updatedHotel.setAddress("789 Updated Avenue");
        updatedHotel.setPhone("555-9012");
        updatedHotel.setEmail("updated@hotel.com");
        
        when(hotelService.getHotelById(1L)).thenReturn(Optional.of(testHotel));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/hotels/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedHotel)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteHotel_AsAdmin() throws Exception {
        // Arrange
        when(hotelService.getHotelById(1L)).thenReturn(Optional.of(testHotel));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(delete("/api/hotels/1"))
            .andExpect(status().isOk());
            
        verify(hotelService, times(1)).deleteHotel(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteHotel_AsUser_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(hotelService.getHotelById(1L)).thenReturn(Optional.of(testHotel));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/api/hotels/1"))
            .andExpect(status().isForbidden());
    }
}